package com.starview.cinemabooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.starview.cinemabooking.model.Phim;

public interface PhimRepository extends JpaRepository<Phim, Integer>, JpaSpecificationExecutor<Phim> {
	List<Phim> findByIsActiveTrue();
}
