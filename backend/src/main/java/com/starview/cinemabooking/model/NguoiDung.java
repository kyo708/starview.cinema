package com.starview.cinemabooking.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NGUOI_DUNG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "ho_ten", nullable = false)
	private String hoTen;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    // Added to support Spring Security RBAC
    @Column(name = "vai_tro", nullable = false)
    private String vaiTro; 
    
    // NEW: Điểm thành viên
    @Column(name = "diem_tich_luy", nullable = false)
    private Integer diemTichLuy = 0;

    // NEW: Optimistic Lock tránh exploit một user double-booking để dùng nhiều điểm hơn mình có 
    @Version
    @Column(name = "phien_ban")
    private Integer phienBan;

    // Relationship: NGUOI_DUNG ||--o{ DON_HANG : "tạo"
    @OneToMany(mappedBy = "nguoiDung", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DonHang> donHangs;
}
