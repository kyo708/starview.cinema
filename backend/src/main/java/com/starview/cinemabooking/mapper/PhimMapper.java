package com.starview.cinemabooking.mapper;

import com.starview.cinemabooking.dtos.PhimDTO;
import com.starview.cinemabooking.model.Phim;

public class PhimMapper {
    public static PhimDTO toDTO(Phim phim) {
        if (phim == null)
            return null;

        return new PhimDTO(
                phim.getId(),
                phim.getTenPhim(),
                phim.getGiaGoc(),
                phim.getThoiLuongPhut(),
                phim.getTrailerUrl(),
                phim.getPosterUrl(),
                phim.getDanhGia(),
                phim.getTheLoai(),
                phim.getMoTa(),
                phim.isActive());
    }

    public static Phim toPhim(PhimDTO phimDto) {
        if (phimDto == null)
            return null;

        Phim phim = new Phim();
        // Notice we DO NOT map the ID here.
        // The database auto-increments and assigns the ID when we save!
        phim.setTenPhim(phimDto.getTenPhim());
        phim.setGiaGoc(phimDto.getGiaGoc());
        phim.setThoiLuongPhut(phimDto.getThoiLuongPhut());
        phim.setTrailerUrl(phimDto.getTrailerUrl());
        phim.setPosterUrl(phimDto.getPosterUrl());
        phim.setDanhGia(phimDto.getDanhGia());
        phim.setTheLoai(phimDto.getTheLoai());
        phim.setMoTa(phimDto.getMoTa());

        // Ensure new movies are active by default
        phim.setActive(true);

        return phim;
    }
}
