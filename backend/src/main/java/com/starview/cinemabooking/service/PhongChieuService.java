package com.starview.cinemabooking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.starview.cinemabooking.dtos.PhongChieuDTO;
import com.starview.cinemabooking.repository.PhongChieuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhongChieuService {
    private final PhongChieuRepository phongChieuRepository;

    public List<PhongChieuDTO> getAllRooms() {
        return phongChieuRepository.findAll().stream()
                .map(room -> new PhongChieuDTO(room.getId(), room.getTenPhong(), room.getTongSoGhe()))
                .toList();
    }
}
