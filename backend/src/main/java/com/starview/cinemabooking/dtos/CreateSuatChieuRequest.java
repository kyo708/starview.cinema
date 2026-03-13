package com.starview.cinemabooking.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSuatChieuRequest {
    @NotNull(message = "phimId is required")
    private Integer phimId;

    @NotNull(message = "phongChieuId is required")
    private Integer phongChieuId;

    @NotNull(message = "thoiGianChieu is required")
    private LocalDateTime thoiGianChieu;

    @NotNull(message = "heSoGia is required")
    @Positive(message = "heSoGia must be greater than 0")
    private Float heSoGia;
}
