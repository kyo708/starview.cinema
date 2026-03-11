package com.starview.cinemabooking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starview.cinemabooking.model.NguoiDung;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
	Optional<NguoiDung> findByEmail(String email);
	Optional<NguoiDung> findBySoDienThoai(String soDienThoai);
}
