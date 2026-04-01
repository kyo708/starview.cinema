package com.starview.cinemabooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.starview.cinemabooking.dtos.CreateSuatChieuRequest;
import com.starview.cinemabooking.dtos.MovieShowtimeItemDTO;
import com.starview.cinemabooking.dtos.MovieShowtimesByDateResponse;
import com.starview.cinemabooking.dtos.SuatChieuCreateResponse;
import com.starview.cinemabooking.dtos.SuatChieuDTO;
import com.starview.cinemabooking.mapper.GheSuatChieuMapper;
import com.starview.cinemabooking.mapper.SuatChieuMapper;
import com.starview.cinemabooking.model.GheSuatChieu;
import com.starview.cinemabooking.model.Phim;
import com.starview.cinemabooking.model.PhongChieu;
import com.starview.cinemabooking.model.SuatChieu;
import com.starview.cinemabooking.repository.GheSuatChieuRepository;
import com.starview.cinemabooking.repository.PhimRepository;
import com.starview.cinemabooking.repository.PhongChieuRepository;
import com.starview.cinemabooking.repository.SuatChieuRepository;
import com.starview.cinemabooking.dtos.GheSuatChieuDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuatChieuService {
    private static final String SEAT_STATUS_TRONG = "TRONG";

    private final SuatChieuRepository suatChieuRepository;
    private final GheSuatChieuRepository gheSuatChieuRepository;
    private final PhimRepository phimRepository;
    private final PhongChieuRepository phongChieuRepository;

    @Transactional(readOnly = true)
    public List<SuatChieuDTO> getAllSuatChieuStaff() {
        List<SuatChieu> entities = suatChieuRepository.findAll();
        return entities.stream()
                .map(SuatChieuMapper::toDTO)
                .collect(Collectors.toList());
    }

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

        int totalCapacity = phongChieu.getTongSoGhe();
        List<GheSuatChieu> gheSuatChieus = new ArrayList<>();
        int seatsGenerated = 0;

        while (seatsGenerated < totalCapacity) {
            GheSuatChieu ghe = new GheSuatChieu();
            ghe.setSuatChieu(savedSuatChieu);
            ghe.setDonHang(null);

            String seatType = determineSeatType(seatsGenerated + 1, totalCapacity, phongChieu.getLoaiPhong());
            ghe.setLoaiGhe(seatType);

            ghe.setTrangThai(SEAT_STATUS_TRONG);
            ghe.setThoiGianHetHanGiuCho(start);
            ghe.setPhienBan(1);
            gheSuatChieus.add(ghe);

            if ("SWEETBOX".equals(seatType)) {
                seatsGenerated += 2; // Ghế đôi chiếm 2 sức chứa
            } else {
                seatsGenerated += 1; // Ghế thường/VIP chiếm 1 sức chứa
            }
        }

        gheSuatChieuRepository.saveAll(gheSuatChieus);

        return new SuatChieuCreateResponse(
                savedSuatChieu.getId(),
                phim.getId(),
                phongChieu.getId(),
                savedSuatChieu.getThoiGianChieu(),
                totalCapacity);
    }

    public MovieShowtimesByDateResponse getMovieShowtimesByDate(Integer phimId, LocalDate date) {
        Phim phim = phimRepository.findById(phimId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phim not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart;

        // Nếu ngày chọn là hôm nay, chỉ lấy các suất từ giờ hiện tại trở đi
        if (date.equals(now.toLocalDate())) {
            dayStart = now;
        } else {
            dayStart = date.atStartOfDay();
        }
        
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
                    availability,
                    showtime.getPhongChieu().getLoaiPhong());
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

    private String determineSeatType(int index, int totalCapacity, String loaiPhong) {
        String type = (loaiPhong != null) ? loaiPhong.trim().toUpperCase() : "2D";
        
        switch (type) {
            case "VIP":

                if (index <= 12) return "THUONG";
                else if (index <= 42) return "VIP";
                else return "SWEETBOX";
            case "IMAX":
                // Ví dụ phòng IMAX (Tổng 80 sức chứa): 20 Thường, 40 VIP, 20 sức chứa cho Sweetbox (10 ghế đôi)
                if (index <= 16) return "THUONG";
                else if (index <= 56) return "VIP";
                else return "SWEETBOX";
                
            case "2D":
            default:
                // Ví dụ phòng 2D (Tổng 100 sức chứa): 40 Thường, 50 VIP, 10 sức chứa cho Sweetbox (5 ghế đôi)
                if (index <= 30) return "THUONG";
                else if (index <= 90) return "VIP";
                else return "SWEETBOX";
        }
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
    @Transactional(readOnly = true)
    public List<GheSuatChieuDTO> getGheBySuatChieu(Integer suatChieuId) {
    // 1. Lấy danh sách ghế từ database
	    List<GheSuatChieu> danhSachGhe = gheSuatChieuRepository.findBySuatChieu_Id(suatChieuId);
	    
	    // 2. Chuyển đổi Entity sang DTO để tránh lộ dữ liệu thừa
	    return danhSachGhe.stream()
	    		.map(GheSuatChieuMapper::toDto)
	    		.collect(Collectors.toList());
	}
    
    // THE FIX: Thêm annotation này để giữ session mở cho Mapper
    @Transactional(readOnly = true)
    public SuatChieuDTO getSuatChieuById(Integer suatChieuId) {
    	SuatChieu suatChieu = suatChieuRepository.findById(suatChieuId)
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy suất chiếu với ID: " + suatChieuId));
    	// Bây giờ Mapper có thể thoải mái lấy dữ liệu Phim và PhongChieu!
    	return SuatChieuMapper.toDTO(suatChieu);
    }
    
    @Transactional
    public void batchUpdateHeSoGia(List<Integer> suatChieuIds, Float newHeSoGia) {
    	if (newHeSoGia == null || newHeSoGia <= 0) {
    		throw new IllegalArgumentException("Hệ số giá không hợp lệ. Phải lớn hơn 0");
    	}
    	
    	List<SuatChieu> suatChieus = suatChieuRepository.findAllById(suatChieuIds);
    	
    	if (suatChieus.isEmpty()) {
    		throw new IllegalArgumentException("Không tìm thấy suất chiếu nào để cập nhật");
    	}
    	
    	for (SuatChieu sc : suatChieus) {
    		sc.setHeSoGia(newHeSoGia);
    	}
    	
    	suatChieuRepository.saveAll(suatChieus);
    }
}
