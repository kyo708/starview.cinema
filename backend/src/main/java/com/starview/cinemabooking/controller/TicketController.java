package com.starview.cinemabooking.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.dtos.CheckoutRequest;
import com.starview.cinemabooking.dtos.EmailTicketRequest;
import com.starview.cinemabooking.dtos.SeatSessionRequest;
import com.starview.cinemabooking.model.DonHang;
import com.starview.cinemabooking.service.EmailService;
import com.starview.cinemabooking.service.TicketService;
import com.starview.cinemabooking.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TicketController {
	private final TicketService ticketService;
	private final EmailService emailService;
	private final VNPayService vnPayService;

    // Khóa ghế tạm thời để người dùng chọn ghế trong UI (US 2.2, 4.1)
    @PostMapping("/hold")
    public ResponseEntity<String> holdGheSuatChieu(@RequestBody SeatSessionRequest request) {
//		JSON body
//    	{
//    		  "seatId": 1
//			  "sessionId": {UUID}    	
//    	}
    	if (request.getSeatId() == null || request.getSessionId() == null || request.getSessionId().isEmpty()) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ (thiếu seatId hoặc sessionId).");
        }

        try {
            ticketService.lockGheSuatChieu(request.getSeatId(), request.getSessionId());
            return ResponseEntity.ok("Ghế đặt mua đã được khóa thành công.");
        } catch (ObjectOptimisticLockingFailureException e) {
            // Catch xung đột @Version
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ghế bạn chọn đã được người khác nhanh tay đặt mất. Vui lòng chọn ghế khác!");
        } catch (IllegalStateException e) {
            // Catch lỗi nghiệp vụ (vd. đặt ghế đã bán rồi)
        	return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    // US 4.1: Hủy khóa ghế (Khi click bỏ chọn ghế trên UI)
    @PostMapping("/release")
    public ResponseEntity<String> releaseGheSuatChieu(@RequestBody SeatSessionRequest request) {
        if (request.getSeatId() == null || request.getSessionId() == null || request.getSessionId().isEmpty()) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ (thiếu seatId hoặc sessionId).");
        }

        try {
            ticketService.unlockGheSuatChieu(request.getSeatId(), request.getSessionId());
            return ResponseEntity.ok("Đã hủy giữ ghế thành công.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    // Endpoint cho US #8 & #9: Xử lý thanh toán và tạo đơn hàng
    // NEW: Xử lý thanh toán qua VNPay
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@RequestBody CheckoutRequest request, HttpServletRequest httpRequest) {
        try {
        	// 1. THE SAFEGUARD: Ngăn chặn lỗi "Ids must not be null"
            if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vui lòng chọn ít nhất một ghế trước khi thanh toán.");
            }
            // 2. Tạo đơn hàng PENDING (Ghế vẫn giữ trạng thái DANG_CHO)
            DonHang donHang = ticketService.createPendingOrder(request);
            
            // 3. Chuẩn bị dữ liệu cho VNPay
            long amount = Math.round(donHang.getTongTien()); 
            String orderInfo = "Thanh toan ve phim. Ma don hang DH" + donHang.getId();
            String orderId = String.valueOf(donHang.getId());

            // THE FIX: Fetch the exact expiration time for this specific order
            LocalDateTime expireDate = ticketService.getSeatExpirationForOrder(donHang.getId());

            // 4. Gọi Service sinh URL bảo mật (Now passing expireDate!)
            String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, orderId, expireDate, httpRequest);
            
            // 5. Trả URL về cho React frontend để thực hiện redirect (window.location.href)
            return ResponseEntity.ok(Map.of(
                "message", "Đang chuyển hướng đến cổng thanh toán VNPay...",
                "paymentUrl", paymentUrl,
                "bookingRef", "DH" + donHang.getId()
            ));
            
        } catch (IllegalStateException e) {
            // Catches if seats expired while they were typing their credit card
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Lỗi hệ thống mã hóa hash của VNPay
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống khi tạo URL thanh toán.");
        }
    }
    
    @PostMapping("/email/ticket")
    public ResponseEntity<String> sendEmail(@RequestBody EmailTicketRequest request) {
        emailService.sendTicketEmail(request);
        return ResponseEntity.ok("Email đang được gửi đi!");
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<Map<String, String>> getOrderStatus(@PathVariable Integer orderId) {
        String status = ticketService.getOrderStatus(orderId);
        return ResponseEntity.ok(Map.of("status", status));
    }
}
