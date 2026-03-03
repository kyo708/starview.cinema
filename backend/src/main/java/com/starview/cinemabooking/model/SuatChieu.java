package com.starview.cinemabooking.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SUAT_CHIEU")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuatChieu {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relationship: PHIM ||--o{ SUAT_CHIEU
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phim_id", nullable = false)
    private Phim phim;

    // Relationship: PHONG_CHIEU ||--o{ SUAT_CHIEU
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_chieu_id", nullable = false)
    private PhongChieu phongChieu;

    @Column(name = "thoi_gian_chieu", nullable = false)
    private LocalDateTime thoiGianChieu;

    @Column(name = "he_so_gia", nullable = false)
    private Float heSoGia;

    // Relationship: SUAT_CHIEU ||--o{ GHE_SUAT_CHIEU : "bao gồm"
    @OneToMany(mappedBy = "suatChieu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GheSuatChieu> gheSuatChieus;
}
