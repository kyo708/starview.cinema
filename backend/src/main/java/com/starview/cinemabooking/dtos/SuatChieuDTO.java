package com.starview.cinemabooking.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuatChieuDTO {
    private Integer id;
	
	private Integer phimId;
	
	private String tenPhim;

    private Integer phongChieuId;
    
    private String tenPhong;

    private LocalDateTime thoiGianChieu;

    private Float heSoGia;
}
