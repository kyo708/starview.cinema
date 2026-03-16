package com.starview.cinemabooking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.starview.cinemabooking.model.GheSuatChieu;

@Repository
public interface GheSuatChieuRepository extends JpaRepository<GheSuatChieu, Integer> {
	//Tìm danh sách ghế "Đang chờ" và đã hết hạn giữ chỗ
	List<GheSuatChieu> findByTrangThaiAndThoiGianHetHanGiuChoBefore(String trangThai, LocalDateTime currentTime);
    long countBySuatChieuIdAndTrangThaiIgnoreCase(Integer suatChieuId, String trangThai);
    //  tìm ghế theo ID của suất chiếu
    List<GheSuatChieu> findBySuatChieu_Id(Integer suatChieuId);
}
