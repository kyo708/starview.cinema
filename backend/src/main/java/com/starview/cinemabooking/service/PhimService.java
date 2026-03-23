package com.starview.cinemabooking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.starview.cinemabooking.dtos.PhimDTO;
import com.starview.cinemabooking.mapper.PhimMapper;
import com.starview.cinemabooking.model.Phim;
import com.starview.cinemabooking.repository.PhimRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class PhimService {
        private final PhimRepository repository;

        public List<PhimDTO> getActiveMovies() {
                return repository.findByIsActiveTrue()
                                .stream()
                                .map(PhimMapper::toDTO)
                                .collect(Collectors.toList());
        }
        
        // Fetch a single movie to populate the frontend Edit form
        public PhimDTO getMovieById(Integer id) {
            Phim phim = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Movie not found with ID: " + id));
            return PhimMapper.toDTO(phim);
        }

        // #19: Create new movie
        public PhimDTO createMovie(PhimDTO phimDTO) {
                Phim phim = PhimMapper.toPhim(phimDTO);
                Phim savedPhim = repository.save(phim);
                return PhimMapper.toDTO(savedPhim);
        }

        // #19: Update existing movie
        public PhimDTO updateMovie(Integer id, PhimDTO phimDTO) {
                Phim existingPhim = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Phim not found with id: " + id));

                // Update fields
                existingPhim.setTenPhim(phimDTO.getTenPhim());
                existingPhim.setGiaGoc(phimDTO.getGiaGoc());
                existingPhim.setThoiLuongPhut(phimDTO.getThoiLuongPhut());
                existingPhim.setTrailerUrl(phimDTO.getTrailerUrl());
                existingPhim.setPosterUrl(phimDTO.getPosterUrl());
                existingPhim.setDanhGia(phimDTO.getDanhGia());
                existingPhim.setTheLoai(phimDTO.getTheLoai());

                Phim updatedPhim = repository.save(existingPhim);
                return PhimMapper.toDTO(updatedPhim);
        }

        // #20: Disable movie (soft delete)
        public void disableMovie(Integer id) {
                Phim phim = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Phim not found with id: " + id));

                phim.setActive(false);
                repository.save(phim);
        }

        // Get all movies (including inactive) for staff management
        public List<PhimDTO> getAllMovies() {
                return repository.findAll()
                                .stream()
                                .map(PhimMapper::toDTO)
                                .collect(Collectors.toList());
        }

        // #21: Hard delete movie (permanently remove from DB)
        public void deleteMovie(Integer id) {
            if (!repository.existsById(id)) {
                throw new RuntimeException("Phim not found with id: " + id);
            }
            repository.deleteById(id);
        }

        // #22: Restore movie (undo soft delete)
        public void restoreMovie(Integer id) {
                Phim phim = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Phim not found with id: " + id));
                phim.setActive(true);
                repository.save(phim);
        }

        // #66: Search movies by keyword and/or genre
        public List<PhimDTO> searchMovies(String keyword, String theLoai) {
                Specification<Phim> spec = Specification.where((root, query, cb) -> cb.isTrue(root.get("isActive")));

                if (keyword != null && !keyword.isEmpty()) {
                        spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("tenPhim")), "%" + keyword.toLowerCase() + "%"));
                }

                if (theLoai != null && !theLoai.isEmpty()) {
                        spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("theLoai")), "%" + theLoai.toLowerCase() + "%"));
                }

                return repository.findAll(spec)
                                .stream()
                                .map(PhimMapper::toDTO)
                                .collect(Collectors.toList());
        }
}
