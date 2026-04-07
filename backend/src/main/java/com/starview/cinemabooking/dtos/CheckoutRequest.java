package com.starview.cinemabooking.dtos;

import java.util.List;

import lombok.Data;

@Data
public class CheckoutRequest {
	private List<Integer> seatIds;

	private String sessionId;

	private String email;

	private String phone;

	private String cardNumber;

	private List<String> seatNames;

	private String voucherCode;
    
	private String danhSachDichVu;  // vd., "Combo Solo x1, Milo x1"
    
	private Integer tongDiemSuDung; // vd., 450
}
