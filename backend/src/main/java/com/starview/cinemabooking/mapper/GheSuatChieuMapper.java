package com.starview.cinemabooking.mapper;

import com.starview.cinemabooking.dtos.GheSuatChieuDTO;
import com.starview.cinemabooking.model.GheSuatChieu;

public class GheSuatChieuMapper {
	public static GheSuatChieuDTO toDto(GheSuatChieu gheSuatChieu) {
		if(gheSuatChieu == null) {
			return null;
		}
		
		GheSuatChieuDTO dto = new GheSuatChieuDTO();
        dto.setId(gheSuatChieu.getId());
        dto.setLoaiGhe(gheSuatChieu.getLoaiGhe());
        dto.setTrangThai(gheSuatChieu.getTrangThai());
        
        // Safely extract the ID from the related SuatChieu object
        if (gheSuatChieu.getSuatChieu() != null) {
            dto.setSuatChieuId(gheSuatChieu.getSuatChieu().getId());
        }
        
        return dto;
	}
	
	
}
