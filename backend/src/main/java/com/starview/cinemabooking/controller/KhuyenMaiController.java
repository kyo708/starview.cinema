package com.starview.cinemabooking.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.dtos.VoucherApplyResult;
import com.starview.cinemabooking.dtos.VoucherPreviewRequest;
import com.starview.cinemabooking.service.KhuyenMaiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class KhuyenMaiController {
    
    private final KhuyenMaiService khuyenMaiService;

    @PostMapping("/preview")
    public ResponseEntity<?> previewVoucher(@RequestBody VoucherPreviewRequest request) {
        try {
            VoucherApplyResult result = khuyenMaiService.previewVoucher(request.getVoucherCode(), request.getOriginalPrice());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}