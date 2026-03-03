package com.starview.cinemabooking.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PHIM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Phim {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_phim", nullable = false)
    private String tenPhim;

    @Column(name = "gia_goc", nullable = false)
    private Float giaGoc;

    @Column(name = "thoi_luong_phut", nullable = false)
    private Integer thoiLuongPhut;
    
    @Column(name = "trailer_url")
    private String trailerUrl;
    
    // Default to true so new movies show up automatically
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Relationship: PHIM ||--o{ SUAT_CHIEU : "có"
    @OneToMany(mappedBy = "phim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SuatChieu> suatChieus;
}
