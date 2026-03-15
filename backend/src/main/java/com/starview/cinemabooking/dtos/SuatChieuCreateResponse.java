package com.starview.cinemabooking.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuatChieuCreateResponse {
    private Integer suatChieuId;
    private Integer phimId;
    private Integer phongChieuId;
    private LocalDateTime thoiGianChieu;
    private Integer soLuongGheDuocTao;
}
