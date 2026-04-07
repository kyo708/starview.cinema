package com.starview.cinemabooking.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starview.cinemabooking.dtos.CheckoutRequest;
import com.starview.cinemabooking.dtos.EmailTicketRequest;
import com.starview.cinemabooking.dtos.VoucherApplyResult;
import com.starview.cinemabooking.model.DonHang;
import com.starview.cinemabooking.model.GheSuatChieu;
import com.starview.cinemabooking.model.Phim;
import com.starview.cinemabooking.model.SuatChieu;
import com.starview.cinemabooking.repository.DonHangRepository;
import com.starview.cinemabooking.repository.GheSuatChieuRepository;
import com.starview.cinemabooking.repository.NguoiDungRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j // For logging background tasks
public class TicketService {
    private final GheSuatChieuRepository gheSuatChieuRepository;
    private final DonHangRepository donHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final EmailService emailService;
    private final KhuyenMaiService khuyenMaiService;

    // US 2.2
    // #28: Chuyển trạng thái ghế và đặt thời gian giữ chỗ để thanh toán
    // #29: Tránh lỗi double booking, optimistic lock
    // US 4.1
    // #59: Khóa ghế theo Session ID
    @Transactional
    public void lockGheSuatChieu(Integer seatId, String sessionId) {
        GheSuatChieu ghe = gheSuatChieuRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));
		
		    if (!"TRONG".equals(ghe.getTrangThai())) {
            // Idempotency: Nếu chính user này đã khóa ghế này rồi, thì bỏ qua không ném lỗi
            if ("DANG_CHO".equals(ghe.getTrangThai()) && Objects.equals(sessionId, ghe.getPhienGiaoDich())) {
                return; 
            }
            throw new IllegalStateException("Ghế này đã được người khác đặt. Vui lòng chọn ghế khác!");
        }

        ghe.setTrangThai("DANG_CHO");
        ghe.setPhienGiaoDich(sessionId); // Gắn cờ sở hữu cho trình duyệt này
        ghe.setThoiGianHetHanGiuCho(LocalDateTime.now().plusMinutes(5));

        try {
            gheSuatChieuRepository.save(ghe);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("Có người khác đặt trùng ghế này cùng tích tắc này với bạn");
        }
    }

    // US 4.1: Mở khóa ghế khi user bỏ chọn (Single Click)
    @Transactional
    public void unlockGheSuatChieu(Integer seatId, String sessionId) {
        GheSuatChieu ghe = gheSuatChieuRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));

        // Chỉ cho phép mở khóa nếu ghế đang chờ VÀ thuộc về chính session này
        if ("DANG_CHO".equals(ghe.getTrangThai()) && Objects.equals(sessionId, ghe.getPhienGiaoDich())) {
            ghe.setTrangThai("TRONG");
            ghe.setPhienGiaoDich(null);
            ghe.setThoiGianHetHanGiuCho(null);

            gheSuatChieuRepository.save(ghe);
        } else {
            throw new IllegalStateException("Bạn không có quyền bỏ giữ ghế này.");
        }
    }

    // #30: Background task mở lại ghế đã quá thời gian giữ chỗ
    // 60000 milli-giây
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void moGheHetHan() {
        LocalDateTime now = LocalDateTime.now();
        List<GheSuatChieu> listGheHetHan = gheSuatChieuRepository
                .findByTrangThaiAndThoiGianHetHanGiuChoBefore("DANG_CHO", now);

        if (!listGheHetHan.isEmpty()) {
            for (GheSuatChieu ghe : listGheHetHan) {
                ghe.setTrangThai("TRONG");
                ghe.setPhienGiaoDich(null);
                ghe.setThoiGianHetHanGiuCho(null);
             
                // THE FIX: Fail the abandoned order to unlock the user's voucher and points!
                DonHang abandonedOrder = ghe.getDonHang();
                if (abandonedOrder != null && "PENDING".equals(abandonedOrder.getTrangThaiThanhToan())) {
                    abandonedOrder.setTrangThaiThanhToan("FAILED");
                    donHangRepository.save(abandonedOrder);
                }
                
                ghe.setDonHang(null);
            }
            gheSuatChieuRepository.saveAll(listGheHetHan);
            log.info("Mở {} ghế hết hạn giữ chỗ", listGheHetHan.size());
        }
    }

    @Transactional
    public DonHang processPaymentAndSaveOrder(CheckoutRequest request) {
        // 1. Tìm tất cả ghế đặt mua
        List<GheSuatChieu> seats = gheSuatChieuRepository.findAllById(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new IllegalArgumentException("Một số ghế không tồn tại trong hệ thống.");
        }

        // 2. Kiểm tra ghế có phải đang ở trạng thái "Đang chờ" VÀ thuộc session này
        // không
        for (GheSuatChieu ghe : seats) {
            if (!"DANG_CHO".equals(ghe.getTrangThai()) || !Objects.equals(request.getSessionId(), ghe.getPhienGiaoDich())) {
                throw new IllegalStateException("Ghế " + ghe.getId() + " đã hết thời gian giữ chỗ hoặc đã bị mua. Trạng thái hiện tại: " + ghe.getTrangThai());
            }
        }

        // 3. (US #8 - AC #34): backend tính tổng tiền
        float totalPrice = 0.0f;

        for (GheSuatChieu ghe : seats) {
            // Chỉ cần gọi hàm tự tính tiền của cái ghế đó!
            totalPrice += ghe.calculatePrice();
        }

        // 3.1 Áp dụng voucher nếu có (US voucher)
        VoucherApplyResult voucherResult = khuyenMaiService.applyVoucher(request.getVoucherCode(), totalPrice);
        // 4. Tạo đơn hàng (US #9 - AC #38)
        DonHang donHang = new DonHang();

        attachAuthenticatedNguoiDung(donHang);

        donHang.setEmailKhachHang(request.getEmail());
        donHang.setSdtKhachHang(request.getPhone());
        donHang.setTongTienGoc(voucherResult.getOriginalPrice());
        donHang.setTongTien(voucherResult.getDiscountedPrice());
        donHang.setKhuyenMai(voucherResult.getKhuyenMai());
        donHang.setTrangThaiThanhToan("SUCCESS"); 
        donHang.setThoiGianTao(LocalDateTime.now());

        // Lưu và lấy id Đơn hàng
        donHang = donHangRepository.save(donHang);

        // 5. UPDATE ghế (US #9 - AC #39)
        for (GheSuatChieu ghe : seats) {
            ghe.setDonHang(donHang);
            ghe.setTrangThai("DA_BAN");
            ghe.setPhienGiaoDich(null);
            ghe.setThoiGianHetHanGiuCho(null);
        }

        gheSuatChieuRepository.saveAll(seats);

        // 6. Gửi email
        // 1. Extract basic details
        String movieName = seats.get(0).getSuatChieu().getPhim().getTenPhim();

        // FIX: Format the Date nicely (e.g., "19:30 - 26/03/2026")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
        String showtime = seats.get(0).getSuatChieu().getThoiGianChieu().format(formatter);
        
        // 2. Format the Seat Names (e.g., "A1, A2")
        String joinedSeatNames = String.join(", ", request.getSeatNames());

        // 3. Generate the Booking Reference (Matching your format)
        String bookingRef = "DH" + donHang.getId();

        // 4. Build the QR Code URL exactly like the Frontend
        // Format: REF:DH5|MOVIE:Dune|SEATS:A1,A2
        String rawQrData = "REF:" + bookingRef + "|MOVIE:" + movieName + "|SEATS:" + joinedSeatNames;
        String encodedQrData = URLEncoder.encode(rawQrData, StandardCharsets.UTF_8);
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedQrData + "&color=000000&bgcolor=ffffff";
        
        // FIX: Format Price to remove the trailing ".0" and add commas
        String formattedPrice = String.format("%,.0f", donHang.getTongTien());

        // 5. Populate the Email Request
        EmailTicketRequest emailReq = new EmailTicketRequest();
        emailReq.setTo_email(request.getEmail());
        emailReq.setCustomer_phone(request.getPhone());
        emailReq.setMovie_name(movieName);
        emailReq.setShowtime(showtime);
        emailReq.setSeats(joinedSeatNames);

        // Format price properly
        emailReq.setTotal_price(formattedPrice + " ₫");
        emailReq.setBooking_ref(bookingRef);
        emailReq.setQr_image_link(qrUrl);

        // 6. Fire the Async Email!
        emailService.sendTicketEmail(emailReq);

        log.info("Successfully created DonHang ID: {} for {} with total: {}", donHang.getId(), request.getEmail(),
                donHang.getTongTien());

        return donHang;
	  }
	
	  @Transactional
    public DonHang createPendingOrder(CheckoutRequest request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
        }

        List<GheSuatChieu> seats = gheSuatChieuRepository.findAllById(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new IllegalArgumentException("Một số ghế không tồn tại.");
        }

        for (GheSuatChieu ghe : seats) {
            if (!"DANG_CHO".equals(ghe.getTrangThai()) || !Objects.equals(request.getSessionId(), ghe.getPhienGiaoDich())) {
                throw new IllegalStateException("Ghế " + ghe.getId() + " đã bị mất quyền giữ chỗ.");
            }
        }

        float totalPrice = 0.0f;
        for (GheSuatChieu ghe : seats) {
            totalPrice += ghe.calculatePrice();
        }
        
        // 2. THE FIX: Áp dụng voucher ở đây!
        VoucherApplyResult voucherResult = khuyenMaiService.applyVoucher(request.getVoucherCode(), totalPrice);

        DonHang donHang = new DonHang();

        attachAuthenticatedNguoiDung(donHang);

        donHang.setEmailKhachHang(request.getEmail());
        donHang.setSdtKhachHang(request.getPhone());

        // Lưu giá gốc, giá sau giảm và mã voucher đã dùng
        donHang.setTongTienGoc(voucherResult.getOriginalPrice());
        donHang.setTongTien(voucherResult.getDiscountedPrice());
        donHang.setKhuyenMai(voucherResult.getKhuyenMai());
        
        // CRITICAL CHANGE: Status is now PENDING
        donHang.setTrangThaiThanhToan("PENDING"); 
        donHang.setThoiGianTao(LocalDateTime.now());
        // Save the seat names to the database!
        donHang.setDanhSachGhe(String.join(", ", request.getSeatNames()));
        
        donHang = donHangRepository.save(donHang);

        for (GheSuatChieu ghe : seats) {
            // Link the seat to the pending order, but DO NOT change status to DA_BAN yet!
            ghe.setDonHang(donHang); 
        }
        gheSuatChieuRepository.saveAll(seats);
        
        log.info("Created PENDING DonHang ID: {}", donHang.getId());
        return donHang;
    }
	
	  @Transactional
    public void finalizeOrderSuccess(Integer orderId) {
        DonHang donHang = donHangRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        // Protect against duplicate webhook calls from VNPay
        if ("SUCCESS".equals(donHang.getTrangThaiThanhToan())) {
            log.info("Order {} is already SUCCESS. Ignoring webhook.", orderId);
            return;
        }

        // 1. Mark Order as Paid
        donHang.setTrangThaiThanhToan("SUCCESS");
        donHangRepository.save(donHang);

        // 2. Lock the Seats Officially
        List<GheSuatChieu> seats = gheSuatChieuRepository.findByDonHang(donHang);
        for (GheSuatChieu ghe : seats) {
            ghe.setTrangThai("DA_BAN");    
            ghe.setPhienGiaoDich(null);
            ghe.setThoiGianHetHanGiuCho(null);   
        }
        gheSuatChieuRepository.saveAll(seats);

        // 3. Fire the Email
        String movieName = seats.get(0).getSuatChieu().getPhim().getTenPhim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
        String showtime = seats.get(0).getSuatChieu().getThoiGianChieu().format(formatter);
        
        String joinedSeatNames = donHang.getDanhSachGhe();
        String bookingRef = "DH" + donHang.getId();

        String rawQrData = "REF:" + bookingRef + "|MOVIE:" + movieName + "|SEATS:" + joinedSeatNames;
        String encodedQrData = URLEncoder.encode(rawQrData, StandardCharsets.UTF_8);
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedQrData + "&color=000000&bgcolor=ffffff";

        String formattedPrice = String.format("%,.0f", donHang.getTongTien());

        EmailTicketRequest emailReq = new EmailTicketRequest();
        emailReq.setTo_email(donHang.getEmailKhachHang());
        emailReq.setCustomer_phone(donHang.getSdtKhachHang());
        emailReq.setMovie_name(movieName);
        emailReq.setShowtime(showtime);
        emailReq.setSeats(joinedSeatNames);
        emailReq.setTotal_price(formattedPrice + " ₫");
        emailReq.setBooking_ref(bookingRef);
        emailReq.setQr_image_link(qrUrl);

        emailService.sendTicketEmail(emailReq);
        log.info("Order {} finalized successfully. Email triggered.", orderId);
    }
	
	  @Transactional
    public void finalizeOrderFailed(Integer orderId) {
        DonHang donHang = donHangRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        donHang.setTrangThaiThanhToan("FAILED");
        donHangRepository.save(donHang);

        // Release the seats back to the public pool
        List<GheSuatChieu> seats = gheSuatChieuRepository.findByDonHang(donHang);
        for (GheSuatChieu ghe : seats) {
            ghe.setTrangThai("TRONG");    
            ghe.setPhienGiaoDich(null);
            ghe.setThoiGianHetHanGiuCho(null);
            ghe.setDonHang(null); // Unlink from the failed order
        }
        gheSuatChieuRepository.saveAll(seats);
        log.warn("Order {} failed/canceled. Seats released.", orderId);
    }

    @Transactional(readOnly = true)
    public String getOrderStatus(Integer orderId) {
        DonHang donHang = donHangRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        return donHang.getTrangThaiThanhToan();
    }

    @Transactional(readOnly = true)
    public LocalDateTime getSeatExpirationForOrder(Integer orderId) {
        DonHang donHang = donHangRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        List<GheSuatChieu> seats = donHang.getGheSuatChieus();
        
        if (seats == null || seats.isEmpty()) {
            log.warn("Đơn hàng PENDING ID: {} không có ghế nào được liên kết. Sử dụng thời gian hết hạn mặc định.", orderId);
            return LocalDateTime.now().plusMinutes(5); 
        }
        
        // Tất cả các ghế trong một đơn hàng đều có cùng thời gian hết hạn
        return seats.get(0).getThoiGianHetHanGiuCho();
    }

    private void attachAuthenticatedNguoiDung(DonHang donHang) {
        if (donHang == null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return;
        }

        nguoiDungRepository.findByEmail(email).ifPresent(donHang::setNguoiDung);
    }
}
