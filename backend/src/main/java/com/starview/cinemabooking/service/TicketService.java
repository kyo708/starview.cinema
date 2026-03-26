package com.starview.cinemabooking.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starview.cinemabooking.dtos.CheckoutRequest;
import com.starview.cinemabooking.dtos.EmailTicketRequest;
import com.starview.cinemabooking.model.DonHang;
import com.starview.cinemabooking.model.GheSuatChieu;
import com.starview.cinemabooking.model.Phim;
import com.starview.cinemabooking.model.SuatChieu;
import com.starview.cinemabooking.repository.DonHangRepository;
import com.starview.cinemabooking.repository.GheSuatChieuRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j // For logging background tasks
public class TicketService {
	private final GheSuatChieuRepository gheSuatChieuRepository;
	private final DonHangRepository donHangRepository;
	private final EmailService emailService;
	
	// US 2.2
	// #28: Chuyển trạng thái ghế và đặt thời gian giữ chỗ để thanh toán 
	// #29: Tránh lỗi double booking, optimistic lock
	// US 4.1
	// #59: Khóa ghế theo Session ID 
	@Transactional
	public void lockGheSuatChieu(Integer seatId, String sessionId) {
		GheSuatChieu ghe = gheSuatChieuRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));
		
		if (!ghe.getTrangThai().equals("TRONG")) {
            // Idempotency: Nếu chính user này đã khóa ghế này rồi, thì bỏ qua không ném lỗi
            if (ghe.getTrangThai().equals("DANG_CHO") && ghe.getPhienGiaoDich().equals(sessionId)) {
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
        if (ghe.getTrangThai().equals("DANG_CHO") && ghe.getPhienGiaoDich().equals(sessionId)) {
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
		List<GheSuatChieu> listGheHetHan = gheSuatChieuRepository.findByTrangThaiAndThoiGianHetHanGiuChoBefore("DANG_CHO", now);
		
		if (!listGheHetHan.isEmpty()) {
			for (GheSuatChieu ghe : listGheHetHan) {
				ghe.setTrangThai("TRONG");
				ghe.setPhienGiaoDich(null);
				ghe.setThoiGianHetHanGiuCho(null);
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

        // 2. Kiểm tra ghế có phải đang ở trạng thái "Đang chờ" VÀ thuộc session này không
        for (GheSuatChieu ghe : seats) {
            if (!ghe.getTrangThai().equals("DANG_CHO") || !ghe.getPhienGiaoDich().equals(request.getSessionId())) {
                throw new IllegalStateException("Ghế " + ghe.getId() + " đã hết thời gian giữ chỗ hoặc đã bị mua. Trạng thái hiện tại: " + ghe.getTrangThai());
            }
        }

        // 3. (US #8 - AC #34): backend tính tổng tiền
    	float totalPrice = 0.0f;

		for (GheSuatChieu ghe : seats) {
			// Chỉ cần gọi hàm tự tính tiền của cái ghế đó!
   	 		totalPrice += ghe.calculatePrice();
		}
        // 4. Tạo đơn hàng (US #9 - AC #38)
        DonHang donHang = new DonHang();
        donHang.setEmailKhachHang(request.getEmail());
        donHang.setSdtKhachHang(request.getPhone());
        donHang.setTongTien(totalPrice);
        donHang.setTrangThaiThanhToan("SUCCESS"); // Mock payment tạm giả sử auto thành công
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
        String showtime = seats.get(0).getSuatChieu().getThoiGianChieu().toString();
        
        // 2. Format the Seat Names (e.g., "A1, A2")
        String joinedSeatNames = String.join(", ", request.getSeatNames());
        
        // 3. Generate the Booking Reference (Matching your format)
        String bookingRef = "DH" + donHang.getId();

        // 4. Build the QR Code URL exactly like the Frontend
        // Format: REF:DH5|MOVIE:Dune|SEATS:A1,A2
        String rawQrData = "REF:" + bookingRef + "|MOVIE:" + movieName + "|SEATS:" + joinedSeatNames;
        String encodedQrData = URLEncoder.encode(rawQrData, StandardCharsets.UTF_8);
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedQrData + "&color=000000&bgcolor=ffffff";

        // 5. Populate the Email Request
        EmailTicketRequest emailReq = new EmailTicketRequest();
        emailReq.setTo_email(request.getEmail());
        emailReq.setCustomer_phone(request.getPhone());
        emailReq.setMovie_name(movieName);
        emailReq.setShowtime(showtime);
        emailReq.setSeats(joinedSeatNames);
        
        // Format price properly
        emailReq.setTotal_price(donHang.getTongTien() + " ₫");
        emailReq.setBooking_ref(bookingRef);
        emailReq.setQr_image_link(qrUrl);

        // 6. Fire the Async Email!
        emailService.sendTicketEmail(emailReq);
        

        log.info("Successfully created DonHang ID: {} for {} with total: {}", donHang.getId(), request.getEmail(), totalPrice);
        
        return donHang;
	}
	
}
