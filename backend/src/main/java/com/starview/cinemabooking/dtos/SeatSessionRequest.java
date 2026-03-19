package com.starview.cinemabooking.dtos;

import lombok.Data;

@Data
public class SeatSessionRequest {
	private Integer seatId;
	
	private String sessionId;
}
