package com.starview.cinemabooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.starview.cinemabooking.model.Phim;

public interface PhimRepository extends JpaRepository<Phim, Integer>{
	List<Phim> findByIsActiveTrue();
	// 1. Public: Tìm phim đang chiếu theo Keyword
    List<Phim> findByTenPhimContainingIgnoreCaseAndIsActiveTrue(String keyword);

    // 2. Staff: Câu truy vấn động (Dynamic Query) cho phép lọc theo nhiều điều kiện. 
    // Nếu param nào null thì bỏ qua điều kiện đó (IS NULL).
    @Query("SELECT p FROM Phim p WHERE " +
           "(:name IS NULL OR LOWER(p.tenPhim) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.theLoai) LIKE LOWER(CONCAT('%', :category, '%')))")
    List<Phim> searchByFilter(@Param("name") String name, @Param("category") String category);
}
