package com.starview.cinemabooking.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.service.TicketService;
import com.starview.cinemabooking.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class VNPayController {
	private final VNPayService vnPayService;
	private final TicketService ticketService;
	
	@GetMapping("/create-url")
    public ResponseEntity<?> createPaymentUrl(
            @RequestParam long amount, 
            @RequestParam String orderInfo, 
            @RequestParam String orderId,
            HttpServletRequest request) {

        // 1. Ask the service to do the heavy lifting
        String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, orderId, request);

        // 2. Return the JSON response
        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        
        return ResponseEntity.ok(response);
	}
	
	@GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> queryParams) {
        // 1. Verify the signature to prevent hackers from faking a payment
        boolean isValid = vnPayService.verifySignature(queryParams);
        
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Chữ ký không hợp lệ (Invalid Signature)"));
        }

        // 2. Extract Data
        String orderIdStr = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");
        Integer orderId = Integer.parseInt(orderIdStr);

        // 3. Process the Result
        if ("00".equals(responseCode)) {
            ticketService.finalizeOrderSuccess(orderId);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success", 
                    "message", "Thanh toán thành công", 
                    "orderId", orderIdStr));
        } else {
            ticketService.finalizeOrderFailed(orderId);
            
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed", 
                    "message", "Thanh toán thất bại hoặc bị hủy", 
                    "orderId", orderIdStr));
        }
    }
}
