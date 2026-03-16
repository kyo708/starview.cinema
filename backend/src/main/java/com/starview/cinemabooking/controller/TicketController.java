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
import com.starview.cinemabooking.model.DonHang;
import com.starview.cinemabooking.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TicketController {
	private final TicketService ticketService;

    // Khóa ghế tạm thời để người dùng thanh toán
    @PostMapping("/hold")
    public ResponseEntity<String> holdGheSuatChieu(@RequestBody Map<String, List<Integer>> request) {
//		JSON body
//    	{
//    		  "seatIds": [1]
//    	}
        List<Integer> seatIds = request.get("seatIds");
        
        if (seatIds == null || seatIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng chọn ít nhất 1 ghế");
        }

        try {
            ticketService.lockGheSuatChieu(seatIds);
            return ResponseEntity.ok("Ghế đặt mua đã được khóa trong 5 phút.");
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
