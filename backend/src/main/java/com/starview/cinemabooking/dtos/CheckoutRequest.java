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
}
