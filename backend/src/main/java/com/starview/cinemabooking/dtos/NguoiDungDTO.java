package com.starview.cinemabooking.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDungDTO {
	private Integer id;
	
	private String hoTen;
	
	private String email;
	
	private String matKhau;
	
	private String soDienThoai;
	
	private String vaiTro;
}
