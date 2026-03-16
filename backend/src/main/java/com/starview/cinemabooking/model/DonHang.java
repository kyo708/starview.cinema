package com.starview.cinemabooking.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DON_HANG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonHang {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relationship: NGUOI_DUNG ||--o{ DON_HANG
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dung_id", nullable = true) // Khách mua vé không cần TK
    private NguoiDung nguoiDung;
    
    @Column(name = "email_khach_hang", nullable = false)
    private String emailKhachHang;

    @Column(name = "sdt_khach_hang", nullable = false)
    private String sdtKhachHang;

    @Column(name = "tong_tien", nullable = false)
    private Float tongTien;

    // e.g., "Chờ thanh toán", "Thành công", "Thất bại"
    @Column(name = "trang_thai_thanh_toan", length = 50, nullable = false)
    private String trangThaiThanhToan;

    @Column(name = "thoi_gian_tao", nullable = false)
    private LocalDateTime thoiGianTao;

    // Relationship: DON_HANG ||--o{ GHE_SUAT_CHIEU : "chứa"
    @OneToMany(mappedBy = "donHang", fetch = FetchType.LAZY)
    private List<GheSuatChieu> gheSuatChieus;
}
