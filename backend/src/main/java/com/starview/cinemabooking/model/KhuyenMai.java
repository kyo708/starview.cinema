package com.starview.cinemabooking.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.*;

@Entity
@Table(name = "KHUYEN_MAI")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khuyen_mai", nullable = false, unique = true)
    private String maKhuyenMai;

    // "PERCENT" hoặc "FLAT"
    @Column(name = "loai", length = 20, nullable = false)
    private String loai;

    // Giá trị giảm (% hoặc số tiền cố định)
    @Column(name = "gia_tri", nullable = false)
    private Float giaTri;

    // Chỉ áp dụng cho voucher theo %
    @Column(name = "max_giam_gia")
    private Float maxGiamGia;

    @Column(name = "ngay_het_han", nullable = false)
    private LocalDateTime ngayHetHan;

    @Column(name = "gioi_han_su_dung", nullable = false)
    private Integer gioiHanSuDung;

    @Column(name = "da_su_dung", nullable = false)
    private Integer daSuDung = 0;

    // Voucher chỉ dành cho thành viên mới (chỉ áp dụng cho lần mua vé thành công đầu tiên)
    @Column(name = "danh_cho_thanh_vien_moi", nullable = false)
    private boolean danhChoThanhVienMoi = false;

    @Version
    @Column(name = "phien_ban")
    private Integer phienBan;

    @JsonIgnore 
    @OneToMany(mappedBy = "khuyenMai", fetch = FetchType.LAZY)
    private List<DonHang> donHangs;

    
}