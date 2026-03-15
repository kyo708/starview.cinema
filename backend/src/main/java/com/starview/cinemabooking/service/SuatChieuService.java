package com.starview.cinemabooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.starview.cinemabooking.dtos.CreateSuatChieuRequest;
import com.starview.cinemabooking.dtos.MovieShowtimeItemDTO;
import com.starview.cinemabooking.dtos.MovieShowtimesByDateResponse;
import com.starview.cinemabooking.dtos.SuatChieuCreateResponse;
import com.starview.cinemabooking.model.GheSuatChieu;
import com.starview.cinemabooking.model.Phim;
import com.starview.cinemabooking.model.PhongChieu;
import com.starview.cinemabooking.model.SuatChieu;
import com.starview.cinemabooking.repository.GheSuatChieuRepository;
import com.starview.cinemabooking.repository.PhimRepository;
import com.starview.cinemabooking.repository.PhongChieuRepository;
import com.starview.cinemabooking.repository.SuatChieuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuatChieuService {
    private static final String SEAT_STATUS_TRONG = "Trống";

    private final SuatChieuRepository suatChieuRepository;
    private final GheSuatChieuRepository gheSuatChieuRepository;
    private final PhimRepository phimRepository;
    private final PhongChieuRepository phongChieuRepository;

    @Transactional
    public SuatChieuCreateResponse createSuatChieuWithSeats(CreateSuatChieuRequest request) {
        validateCreateRequest(request);

        Phim phim = phimRepository.findById(request.getPhimId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phim not found"));

        PhongChieu phongChieu = phongChieuRepository.findById(request.getPhongChieuId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PhongChieu not found"));

        LocalDateTime start = request.getThoiGianChieu();
        LocalDateTime end = start.plusMinutes(phim.getThoiLuongPhut());

        rejectIfOverlapping(phongChieu.getId(), start, end);

        SuatChieu suatChieu = new SuatChieu();
        suatChieu.setPhim(phim);
        suatChieu.setPhongChieu(phongChieu);
        suatChieu.setThoiGianChieu(start);
        suatChieu.setHeSoGia(request.getHeSoGia());

        SuatChieu savedSuatChieu = suatChieuRepository.save(suatChieu);

        int totalSeats = phongChieu.getTongSoGhe();
        List<GheSuatChieu> gheSuatChieus = new ArrayList<>(totalSeats);

        for (int i = 1; i <= totalSeats; i++) {
            GheSuatChieu ghe = new GheSuatChieu();
            ghe.setSuatChieu(savedSuatChieu);
            ghe.setDonHang(null);
            ghe.setLoaiGhe(determineSeatType(i, totalSeats));
            ghe.setTrangThai(SEAT_STATUS_TRONG);
            ghe.setThoiGianHetHanGiuCho(start);
            ghe.setPhienBan(1);
            gheSuatChieus.add(ghe);
        }

        gheSuatChieuRepository.saveAll(gheSuatChieus);

        return new SuatChieuCreateResponse(
                savedSuatChieu.getId(),
                phim.getId(),
                phongChieu.getId(),
                savedSuatChieu.getThoiGianChieu(),
                totalSeats);
    }

    public MovieShowtimesByDateResponse getMovieShowtimesByDate(Integer phimId, LocalDate date) {
        Phim phim = phimRepository.findById(phimId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phim not found"));

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime nextDayStart = date.plusDays(1).atStartOfDay();

        List<SuatChieu> showtimes = suatChieuRepository
                .findByPhimIdAndThoiGianChieuBetweenOrderByThoiGianChieuAsc(phimId, dayStart, nextDayStart);

        List<MovieShowtimeItemDTO> items = showtimes.stream().map(showtime -> {
            Integer roomCapacity = showtime.getPhongChieu().getTongSoGhe();
            long availableSeats = gheSuatChieuRepository.countBySuatChieuIdAndTrangThaiIgnoreCase(
                    showtime.getId(),
                    SEAT_STATUS_TRONG);

            String availability = resolveAvailabilityStatus(roomCapacity, availableSeats);

            return new MovieShowtimeItemDTO(
                    showtime.getId(),
                    showtime.getPhongChieu().getId(),
                    showtime.getPhongChieu().getTenPhong(),
                    showtime.getThoiGianChieu(),
                    roomCapacity,
                    availableSeats,
                    availability);
        }).toList();

        return new MovieShowtimesByDateResponse(
                phim.getId(),
                phim.getTenPhim(),
                phim.getMoTa(),
                date,
                items);
    }

    private void validateCreateRequest(CreateSuatChieuRequest request) {
        if (request.getThoiGianChieu().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "thoiGianChieu must be in the future");
        }
    }

    private void rejectIfOverlapping(Integer phongChieuId, LocalDateTime newStart, LocalDateTime newEnd) {
        LocalDateTime queryStart = newStart.minusDays(1);
        LocalDateTime queryEnd = newEnd.plusDays(1);

        List<SuatChieu> existingShowtimes = suatChieuRepository
                .findByPhongChieuIdAndThoiGianChieuBetweenOrderByThoiGianChieuAsc(phongChieuId, queryStart, queryEnd);

        for (SuatChieu existing : existingShowtimes) {
            LocalDateTime existingStart = existing.getThoiGianChieu();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getPhim().getThoiLuongPhut());

            boolean overlaps = newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
            if (overlaps) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Showtime overlaps existing showtime in the same room");
            }
        }
    }

    private String determineSeatType(int index, int totalSeats) {
        int vipSeatCount = (int) Math.ceil(totalSeats * 0.2);
        int vipStartIndex = totalSeats - vipSeatCount + 1;
        return index >= vipStartIndex ? "VIP" : "THUONG";
    }

    private String resolveAvailabilityStatus(Integer totalSeats, long availableSeats) {
        if (availableSeats <= 0) {
            return "SOLD_OUT";
        }

        double ratio = (double) availableSeats / totalSeats;
        if (ratio <= 0.2d) {
            return "LIMITED";
        }

        return "AVAILABLE";
    }
}
