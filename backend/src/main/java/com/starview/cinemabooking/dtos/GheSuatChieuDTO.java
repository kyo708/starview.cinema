package com.starview.cinemabooking.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GheSuatChieuDTO {
	private Integer id;
	
	private Integer suatChieuId;
	
	private String loaiGhe;
	
	private String trangThai;
	
	private Float giaTien;
	
	private String phienGiaoDich;
}
