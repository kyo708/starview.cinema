package com.starview.cinemabooking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.dtos.PhongChieuDTO;
import com.starview.cinemabooking.service.PhongChieuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/phong-chieu")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PhongChieuController {
    private final PhongChieuService phongChieuService;

    @GetMapping("/staff")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<PhongChieuDTO>> getAllRoomsForStaff() {
        return ResponseEntity.ok(phongChieuService.getAllRooms());
    }
}
