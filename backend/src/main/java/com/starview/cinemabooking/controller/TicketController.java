package com.starview.cinemabooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.dtos.CheckoutRequest;
import com.starview.cinemabooking.dtos.SeatSessionRequest;
import com.starview.cinemabooking.model.DonHang;
import com.starview.cinemabooking.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TicketController {
	private final TicketService ticketService;

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
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@RequestBody CheckoutRequest request) {
        try {
        	// 1. THE SAFEGUARD: Ngăn chặn lỗi "Ids must not be null"
            if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vui lòng chọn ít nhất một ghế trước khi thanh toán.");
            }
        	
        	// Validate thẻ tín dụng mock (US #9 - AC #36)
            if (request.getCardNumber() == null || !request.getCardNumber().matches("\\d{16}")) {
                return ResponseEntity.badRequest().body("Số thẻ tín dụng không hợp lệ.");
            }
            
            // Gọi Service để tính tiền và lưu database
            DonHang donHang = ticketService.processPaymentAndSaveOrder(request);
            
            // Trả về bookingRef (chính là ID của DonHang) cho màn hình Ticket.jsx
            return ResponseEntity.ok(Map.of(
                "message", "Thanh toán thành công",
                "bookingRef", "DH" + donHang.getId(),
                "totalPrice", donHang.getTongTien()
            ));
            
        } catch (IllegalStateException e) {
            // Catches if seats expired while they were typing their credit card
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
