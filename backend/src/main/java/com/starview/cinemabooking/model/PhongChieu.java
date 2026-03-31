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
	
	// e.g., "2D", "3D", "IMAX", "VIP"
    @Column(name = "loai_phong", length = 50)
    private String loaiPhong = "2D"; // Default value

    // Phụ thu cho loại phòng (VD: Phòng 3D phụ thu 20k)
    @Column(name = "phu_thu", nullable = false)
    private Float phuThu = 0.0f;

	// Relationship: PHONG_CHIEU ||--o{ SUAT_CHIEU : "chứa"
	@OneToMany(mappedBy = "phongChieu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SuatChieu> suatChieus;
}
