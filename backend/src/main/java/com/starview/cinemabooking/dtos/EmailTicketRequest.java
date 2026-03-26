package com.starview.cinemabooking.dtos;

import lombok.Data;

@Data
public class EmailTicketRequest {
	private String to_email;
    private String customer_phone;
    private String movie_name;
    private String showtime;
    private String seats;
    private String total_price;
    private String booking_ref;
    private String qr_image_link;
}
