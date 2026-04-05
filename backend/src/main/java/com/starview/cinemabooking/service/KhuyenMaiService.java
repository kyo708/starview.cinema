package com.starview.cinemabooking.service;

import java.time.LocalDateTime;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starview.cinemabooking.dtos.VoucherApplyResult;
import com.starview.cinemabooking.model.KhuyenMai;
import com.starview.cinemabooking.model.NguoiDung;
import com.starview.cinemabooking.repository.DonHangRepository;
import com.starview.cinemabooking.repository.KhuyenMaiRepository;
import com.starview.cinemabooking.repository.NguoiDungRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KhuyenMaiService {
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final DonHangRepository donHangRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Transactional
    public VoucherApplyResult applyVoucher(String voucherCode, float originalPrice) {
        VoucherApplyResult result = new VoucherApplyResult();
        result.setOriginalPrice(originalPrice);

        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            result.setDiscountAmount(0f);
            result.setDiscountedPrice(originalPrice);
            return result;
        }

        KhuyenMai khuyenMai = khuyenMaiRepository.findByMaKhuyenMaiIgnoreCase(voucherCode.trim())
                .orElseThrow(() -> new IllegalStateException("Mã khuyến mãi không hợp lệ."));

        // Welcome voucher: chỉ dành cho tài khoản mua vé lần đầu (đơn SUCCESS đầu tiên)
        validateWelcomeVoucherEligibility(khuyenMai);

        if (khuyenMai.getNgayHetHan() != null && khuyenMai.getNgayHetHan().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Mã khuyến mãi đã hết hạn.");
        }

        if (khuyenMai.getGioiHanSuDung() != null && khuyenMai.getDaSuDung() != null
                && khuyenMai.getDaSuDung() >= khuyenMai.getGioiHanSuDung()) {
            throw new IllegalStateException("Mã khuyến mãi đã hết lượt sử dụng.");
        }

        float discountAmount = calculateDiscountAmount(khuyenMai, originalPrice);
        float discountedPrice = originalPrice - discountAmount;

        khuyenMai.setDaSuDung(khuyenMai.getDaSuDung() == null ? 1 : khuyenMai.getDaSuDung() + 1);

        try {
            khuyenMaiRepository.save(khuyenMai);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("Mã khuyến mãi vừa hết lượt sử dụng. Vui lòng thử lại.");
        }

        result.setDiscountAmount(discountAmount);
        result.setDiscountedPrice(discountedPrice);
        result.setKhuyenMai(khuyenMai);
        return result;
    }

    @Transactional(readOnly = true) // IMPORTANT: Read-only transaction, does not update usage count
    public VoucherApplyResult previewVoucher(String voucherCode, float originalPrice) {
        VoucherApplyResult result = new VoucherApplyResult();
        result.setOriginalPrice(originalPrice);

        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            throw new IllegalStateException("Vui lòng nhập mã khuyến mãi.");
        }

        KhuyenMai khuyenMai = khuyenMaiRepository.findByMaKhuyenMaiIgnoreCase(voucherCode.trim())
                .orElseThrow(() -> new IllegalStateException("Mã khuyến mãi không hợp lệ."));

        // Apply the same business rule in preview to avoid inconsistent UX
        validateWelcomeVoucherEligibility(khuyenMai);

        if (khuyenMai.getNgayHetHan() != null && khuyenMai.getNgayHetHan().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Mã khuyến mãi đã hết hạn.");
        }

        if (khuyenMai.getGioiHanSuDung() != null && khuyenMai.getDaSuDung() != null
                && khuyenMai.getDaSuDung() >= khuyenMai.getGioiHanSuDung()) {
            throw new IllegalStateException("Mã khuyến mãi đã hết lượt sử dụng.");
        }

        float discountAmount = calculateDiscountAmount(khuyenMai, originalPrice);
        float discountedPrice = originalPrice - discountAmount;

        // DO NOT increment usage or save entity in a preview

        result.setDiscountAmount(discountAmount);
        result.setDiscountedPrice(discountedPrice);
        return result;
    }

    private void validateWelcomeVoucherEligibility(KhuyenMai khuyenMai) {
        if (khuyenMai == null || !khuyenMai.isDanhChoThanhVienMoi()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Mã giảm giá này chỉ dành cho tài khoản mua vé lần đầu.");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Mã giảm giá này chỉ dành cho tài khoản mua vé lần đầu.");
        }

        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Mã giảm giá này chỉ dành cho tài khoản mua vé lần đầu."));

        // Per-user one-time use: block if this user already used (or is using) this
        // voucher
        long usedOrPendingWithThisVoucher = donHangRepository.countByNguoiDungAndKhuyenMaiAndTrangThaiThanhToanIn(
                nguoiDung,
                khuyenMai,
                java.util.List.of("PENDING", "SUCCESS"));
        if (usedOrPendingWithThisVoucher > 0) {
            throw new IllegalStateException("Mã giảm giá này chỉ dành cho tài khoản mua vé lần đầu.");
        }

        long successfulOrderCount = donHangRepository.countByNguoiDungAndTrangThaiThanhToan(nguoiDung, "SUCCESS");
        if (successfulOrderCount > 0) {
            throw new IllegalStateException("Mã giảm giá này chỉ dành cho tài khoản mua vé lần đầu.");
        }
    }

    private float calculateDiscountAmount(KhuyenMai khuyenMai, float originalPrice) {
        if (khuyenMai.getGiaTri() == null || khuyenMai.getGiaTri() <= 0f) {
            throw new IllegalStateException("Giá trị mã khuyến mãi không hợp lệ.");
        }

        float discountAmount;
        if ("PERCENT".equalsIgnoreCase(khuyenMai.getLoai())) {
            discountAmount = originalPrice * (khuyenMai.getGiaTri() / 100f);
            if (khuyenMai.getMaxGiamGia() != null && khuyenMai.getMaxGiamGia() > 0f) {
                discountAmount = Math.min(discountAmount, khuyenMai.getMaxGiamGia());
            }
        } else if ("FLAT".equalsIgnoreCase(khuyenMai.getLoai())) {
            discountAmount = khuyenMai.getGiaTri();
        } else {
            throw new IllegalStateException("Loại mã khuyến mãi không hợp lệ.");
        }

        return Math.min(discountAmount, originalPrice);
    }
}
