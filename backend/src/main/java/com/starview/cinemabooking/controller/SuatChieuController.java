package com.starview.cinemabooking.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.dtos.CreateSuatChieuRequest;
import com.starview.cinemabooking.dtos.MovieShowtimesByDateResponse;
import com.starview.cinemabooking.dtos.GheSuatChieuDTO;
import com.starview.cinemabooking.dtos.SuatChieuCreateResponse;
import com.starview.cinemabooking.service.SuatChieuService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/suat-chieu")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
public class SuatChieuController {
    private final SuatChieuService suatChieuService;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<SuatChieuCreateResponse> createShowtime(@Valid @RequestBody CreateSuatChieuRequest request) {
        SuatChieuCreateResponse response = suatChieuService.createSuatChieuWithSeats(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/phim/{phimId}")
    public ResponseEntity<MovieShowtimesByDateResponse> getShowtimesByMovieAndDate(
            @PathVariable Integer phimId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(suatChieuService.getMovieShowtimesByDate(phimId, date));
    }

    /**
     * API để lấy danh sách tất cả ghế (kèm trạng thái) của một suất chiếu cụ thể.
     * @param suatChieuId ID của Suất Chiếu
     * @return Danh sách các ghế
     */
    @GetMapping("/{suatChieuId}/ghe")
    public ResponseEntity<List<GheSuatChieuDTO>> getGheBySuatChieu(@PathVariable Integer suatChieuId) {
        return ResponseEntity.ok(suatChieuService.getGheBySuatChieu(suatChieuId));
    }
}
