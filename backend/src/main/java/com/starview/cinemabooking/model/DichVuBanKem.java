package com.starview.cinemabooking.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DICH_VU_BAN_KEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DichVuBanKem {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_dich_vu", nullable = false)
    private String tenDichVu;

    @Column(name = "diem_doi", nullable = false)
    private Integer diemDoi; // Số điểm cần để đổi

    @Column(name = "hinh_anh_url")
    private String hinhAnhUrl;
}
