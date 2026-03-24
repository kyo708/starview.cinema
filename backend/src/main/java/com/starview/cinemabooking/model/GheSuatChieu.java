package com.starview.cinemabooking.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GHE_SUAT_CHIEU")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GheSuatChieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relationship: SUAT_CHIEU ||--o{ GHE_SUAT_CHIEU
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suat_chieu_id", nullable = false)
    private SuatChieu suatChieu;

    // Relationship: DON_HANG ||--o{ GHE_SUAT_CHIEU
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_Hang_id", nullable = true)
    private DonHang donHang;

    // "Ví dụ: Thường, VIP, Đôi"
    @Column(name = "loai_ghe", length = 50, nullable = false)
    private String loaiGhe;

    // "Trống, Đang chờ, Đã bán"
    @Column(name = "trang_thai", length = 50, nullable = false)
    private String trangThai;
    
    @Column(name = "thoi_gian_het_han_giu_cho")
    private LocalDateTime thoiGianHetHanGiuCho;
    
    //"Dùng cho Khóa lạc quan chống trùng ghế"
    @Version
    @Column(name = "phien_ban")
    private int phienBan;
    public float calculatePrice() {
	    // Lấy giá gốc của phim nhân với hệ số giá của suất chiếu
	    float basePrice = this.suatChieu.getPhim().getGiaGoc() * this.suatChieu.getHeSoGia();
	    
	    // Cộng phụ thu nếu là ghế VIP
	    if ("VIP".equalsIgnoreCase(this.loaiGhe)) {
	        return basePrice + 30000f; 
	    }
	    
	    return basePrice;
	}
    
    @Column(name = "phien_giao_dich")
    private String phienGiaoDich;
}
