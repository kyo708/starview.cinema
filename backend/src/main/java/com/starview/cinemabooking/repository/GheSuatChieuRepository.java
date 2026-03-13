package com.starview.cinemabooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starview.cinemabooking.model.GheSuatChieu;

public interface GheSuatChieuRepository extends JpaRepository<GheSuatChieu, Integer> {
    long countBySuatChieuIdAndTrangThaiIgnoreCase(Integer suatChieuId, String trangThai);
}
