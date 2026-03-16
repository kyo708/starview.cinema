package com.starview.cinemabooking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starview.cinemabooking.dtos.CheckoutRequest;
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
	
	// US 2.2
	// #28: Chuyển trạng thái ghế và đặt thời gian giữ chỗ để thanh toán 
	// #29: Tránh lỗi double booking, optimistic lock
	@Transactional
	public void lockGheSuatChieu(List<Integer> seatIds) {
		List<GheSuatChieu> listGhe = gheSuatChieuRepository.findAllById(seatIds);
		
		if(listGhe.size() != seatIds.size()) {
			throw new IllegalArgumentException("Một hoặc nhiều ghế được đặt không tồn tại");
		}
		
		for (GheSuatChieu ghe : listGhe) {
			if (!ghe.getTrangThai().equals("TRONG")) {
				throw new IllegalStateException("Ghế " + ghe.getId() + " đã được người khác đặt rồi");
			}
			
			ghe.setTrangThai("DANG_CHO");
			ghe.setThoiGianHetHanGiuCho(LocalDateTime.now().plusMinutes(5));
		}
		
		try {
			gheSuatChieuRepository.saveAll(listGhe);
		} catch (OptimisticLockingFailureException e) {
			throw new IllegalStateException("Có người khác đặt trùng ghế này cùng tích tắc này với bạn");
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

        // 2. Kiểm tra ghế có phải đang ở trạng thái "Đang chờ" không
        for (GheSuatChieu ghe : seats) {
            if (!"DANG_CHO".equals(ghe.getTrangThai())) {
                throw new IllegalStateException("Ghế " + ghe.getId() + " đã hết thời gian giữ chỗ hoặc đã bị mua. Trạng thái hiện tại: " + ghe.getTrangThai());
            }
        }

        // 3. (US #8 - AC #34): backend tính tổng tiền
        SuatChieu suatChieu = seats.get(0).getSuatChieu();
        Phim phim = suatChieu.getPhim();
        float basePrice = phim.getGiaGoc() * suatChieu.getHeSoGia();
        float totalPrice = 0.0f;

        for (GheSuatChieu ghe : seats) {
            float currentSeatPrice = basePrice;
            // 100k vé VIP vs 70k vé Thường
            if ("VIP".equalsIgnoreCase(ghe.getLoaiGhe())) {
                currentSeatPrice += 30000; 
            }
            totalPrice += currentSeatPrice;
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
            ghe.setThoiGianHetHanGiuCho(null);   
        }
        
        gheSuatChieuRepository.saveAll(seats);

        log.info("Successfully created DonHang ID: {} for {} with total: {}", donHang.getId(), request.getEmail(), totalPrice);
        
        return donHang;
	}
	
}
