package com.starview.cinemabooking.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PHONG_CHIEU")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhongChieu {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "ten_phong", nullable = false)
	private String tenPhong;

	@Column(name = "tong_so_ghe", nullable = false)
	private Integer tongSoGhe;

	// Relationship: PHONG_CHIEU ||--o{ SUAT_CHIEU : "chứa"
	@OneToMany(mappedBy = "phongChieu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SuatChieu> suatChieus;
}
