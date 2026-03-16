package com.starview.cinemabooking.mapper;

import org.springframework.stereotype.Component;

import com.starview.cinemabooking.dtos.SuatChieuDTO;
import com.starview.cinemabooking.model.SuatChieu;

@Component
public class SuatChieuMapper {
	public static SuatChieuDTO toDTO(SuatChieu suatChieu) {
		if(suatChieu == null) {
			return null;
		}
		
		return SuatChieuDTO.builder()
                .id(suatChieu.getId())
                .phimId(suatChieu.getPhim().getId())
                .tenPhim(suatChieu.getPhim().getTenPhim())
                .phongChieuId(suatChieu.getPhongChieu().getId())
                .tenPhong(suatChieu.getPhongChieu().getTenPhong())
                .thoiGianChieu(suatChieu.getThoiGianChieu())
                .heSoGia(suatChieu.getHeSoGia())
                .build();
	}
}
