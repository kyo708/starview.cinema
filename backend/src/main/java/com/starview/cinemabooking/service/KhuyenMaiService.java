package com.starview.cinemabooking.service;

import java.time.LocalDateTime;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starview.cinemabooking.dtos.VoucherApplyResult;
import com.starview.cinemabooking.model.KhuyenMai;
import com.starview.cinemabooking.repository.KhuyenMaiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KhuyenMaiService {
    private final KhuyenMaiRepository khuyenMaiRepository;

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
