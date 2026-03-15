package com.starview.cinemabooking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.starview.cinemabooking.model.SuatChieu;

public interface SuatChieuRepository extends JpaRepository<SuatChieu, Integer> {
    @EntityGraph(attributePaths = { "phim", "phongChieu" })
    List<SuatChieu> findByPhongChieuIdAndThoiGianChieuBetweenOrderByThoiGianChieuAsc(
            Integer phongChieuId,
            LocalDateTime start,
            LocalDateTime end);

    @EntityGraph(attributePaths = { "phim", "phongChieu" })
    List<SuatChieu> findByPhimIdAndThoiGianChieuBetweenOrderByThoiGianChieuAsc(
            Integer phimId,
            LocalDateTime start,
            LocalDateTime end);
}
