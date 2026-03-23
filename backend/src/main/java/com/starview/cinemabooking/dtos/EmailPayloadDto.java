package com.starview.cinemabooking.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailPayloadDto {
    private String to;
    private String ticketId;
    private String movieTitle;
    private String showtime;
    private String seats;
    private double totalPrice;
}