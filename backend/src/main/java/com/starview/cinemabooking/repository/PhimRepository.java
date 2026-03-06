package com.starview.cinemabooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starview.cinemabooking.model.Phim;

public interface PhimRepository extends JpaRepository<Phim, Integer>{
	List<Phim> findByIsActiveTrue();
}
