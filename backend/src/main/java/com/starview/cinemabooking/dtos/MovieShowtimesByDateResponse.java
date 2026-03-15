package com.starview.cinemabooking.dtos;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieShowtimesByDateResponse {
    private Integer phimId;
    private String tenPhim;
    private String movieDescription;
    private LocalDate date;
    private List<MovieShowtimeItemDTO> showtimes;
}
