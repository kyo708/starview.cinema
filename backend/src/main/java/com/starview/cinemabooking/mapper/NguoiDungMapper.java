package com.starview.cinemabooking.mapper;

import com.starview.cinemabooking.dtos.NguoiDungDTO;
import com.starview.cinemabooking.model.NguoiDung;

public class NguoiDungMapper {
	public static NguoiDungDTO toDTO(NguoiDung nguoiDung) {
		if (nguoiDung == null) {
			return null;
		}
		return new NguoiDungDTO(
				nguoiDung.getId(),
				nguoiDung.getHoTen(),
				nguoiDung.getEmail(),
				nguoiDung.getMatKhau(),
				nguoiDung.getSoDienThoai(),
				nguoiDung.getVaiTro());
	}
	
	public static NguoiDung toNguoiDung(NguoiDungDTO nguoiDungDto) {
		if (nguoiDungDto == null) {
			return null;
		}
		
		NguoiDung nguoiDung = new NguoiDung();
		nguoiDung.setHoTen(nguoiDungDto.getHoTen());
		nguoiDung.setEmail(nguoiDungDto.getEmail());
		nguoiDung.setSoDienThoai(nguoiDungDto.getSoDienThoai());
		nguoiDung.setMatKhau(nguoiDungDto.getMatKhau());
		nguoiDung.setVaiTro(nguoiDungDto.getVaiTro());
		
		return nguoiDung;
	}
}
