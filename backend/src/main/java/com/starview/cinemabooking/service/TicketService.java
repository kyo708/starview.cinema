package com.starview.cinemabooking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starview.cinemabooking.model.GheSuatChieu;
import com.starview.cinemabooking.repository.GheSuatChieuRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j // For logging background tasks
public class TicketService {
	private final GheSuatChieuRepository gheSuatChieuRepository;
	
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
	
}
