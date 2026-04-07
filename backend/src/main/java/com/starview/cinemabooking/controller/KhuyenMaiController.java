package com.starview.cinemabooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.starview.cinemabooking.dtos.VoucherApplyResult;
import com.starview.cinemabooking.dtos.VoucherPreviewRequest;
import com.starview.cinemabooking.model.KhuyenMai;
import com.starview.cinemabooking.repository.KhuyenMaiRepository;
import com.starview.cinemabooking.service.KhuyenMaiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vouchers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class KhuyenMaiController {
    
    private final KhuyenMaiService khuyenMaiService;
    private final KhuyenMaiRepository khuyenMaiRepository;

    @PostMapping("/preview")
    public ResponseEntity<?> previewVoucher(@RequestBody VoucherPreviewRequest request) {
        try {
            VoucherApplyResult result = khuyenMaiService.previewVoucher(request.getVoucherCode(), request.getOriginalPrice());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- CÁC API DÀNH CHO ADMIN / STAFF QUẢN LÝ ---

    // API lấy danh sách tất cả voucher
    @GetMapping("/staff")
    public ResponseEntity<List<KhuyenMai>> getAllVouchers() {
        return ResponseEntity.ok(khuyenMaiRepository.findAll());
    }

    // API tạo mới voucher
    @PostMapping("/staff")
    public ResponseEntity<?> createVoucher(@RequestBody KhuyenMai khuyenMai) {
        KhuyenMai saved = khuyenMaiRepository.save(khuyenMai);
        return ResponseEntity.ok(saved);
    }

    // API xóa / vô hiệu hóa voucher
    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> disableVoucher(@PathVariable Integer id) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Voucher"));
        
        // Ở đây mình đang dùng xóa cứng (Delete thẳng khỏi DB)
        // Nếu entity KhuyenMai của bạn có thuộc tính trạng thái (ví dụ isActive), bạn có thể chuyển thành xóa mềm
        khuyenMaiRepository.delete(km);
        
        return ResponseEntity.ok(Map.of("message", "Đã xóa voucher thành công"));
    }
}