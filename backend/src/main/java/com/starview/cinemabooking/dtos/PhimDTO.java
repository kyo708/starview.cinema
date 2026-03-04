package com.starview.cinemabooking.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhimDTO {
    private Integer id;

    private String tenPhim;

    private Float giaGoc;

    private Integer thoiLuongPhut;

    private String trailerUrl;

    private Boolean isActive;
}
