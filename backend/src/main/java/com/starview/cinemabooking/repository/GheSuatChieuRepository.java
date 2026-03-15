package com.starview.cinemabooking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starview.cinemabooking.model.GheSuatChieu;

public interface GheSuatChieuRepository extends JpaRepository<GheSuatChieu, Integer> {
	//Tìm danh sách ghế "Đang chờ" và đã hết hạn giữ chỗ
	List<GheSuatChieu> findByTrangThaiAndThoiGianHetHanGiuChoBefore(String trangThai, LocalDateTime currentTime);
    long countBySuatChieuIdAndTrangThaiIgnoreCase(Integer suatChieuId, String trangThai);
}
