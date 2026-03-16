package com.starview.cinemabooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.starview.cinemabooking.model.DonHang;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
	List<DonHang> findByEmailKhachHang(String emailKhachHang);
	List<DonHang> findBySdtKhachHang(String sdtKhachHang);
}
