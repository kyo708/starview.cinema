package com.starview.cinemabooking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.model.DichVuBanKem;
import com.starview.cinemabooking.repository.DichVuBanKemRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dich-vu")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DichVuBanKemController {
    private final DichVuBanKemRepository dichVuBanKemRepository;

    @GetMapping
    public ResponseEntity<List<DichVuBanKem>> getAllDichVu() {
        return ResponseEntity.ok(dichVuBanKemRepository.findAll());
    }
}