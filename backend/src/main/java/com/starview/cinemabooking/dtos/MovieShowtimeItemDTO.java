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
public class MovieShowtimeItemDTO {
    private Integer suatChieuId;
    private Integer phongChieuId;
    private String tenPhong;
    private LocalDateTime thoiGianChieu;
    private Integer tongSoGhe;
    private Long soGheTrong;
    private String availabilityStatus;
}
