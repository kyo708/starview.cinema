package com.starview.cinemabooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.starview.cinemabooking.dtos.PhimDTO;
import com.starview.cinemabooking.service.PhimService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/phim")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PhimController {
	private final PhimService phimService;

	// Public endpoint - Get active movies for customers
	@GetMapping
	public ResponseEntity<List<PhimDTO>> getPhimDangChieu() {
		return ResponseEntity.ok(phimService.getActiveMovies());
	}
	
	// Public endpoint - Get a specific movie by ID for hydration/details
	@GetMapping("/{id}")
	public ResponseEntity<PhimDTO> getMovieById(@PathVariable Integer id) {
		return ResponseEntity.ok(phimService.getMovieById(id));
	}

	// Staff endpoints - Manage movie catalog

	// Get all movies (including inactive) for staff management
	@GetMapping("/staff")
	@PreAuthorize("hasRole('STAFF')")
	public ResponseEntity<List<PhimDTO>> getAllMovies() {
		return ResponseEntity.ok(phimService.getAllMovies());
	}

	// #19: Create new movie
	@PostMapping("/staff")
	@PreAuthorize("hasRole('STAFF')")
	public ResponseEntity<PhimDTO> createMovie(@RequestBody PhimDTO phimDTO) {
		PhimDTO createdMovie = phimService.createMovie(phimDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
	}

	// #19: Update existing movie
	@PutMapping("/staff/{id}")
	@PreAuthorize("hasRole('STAFF')")
	public ResponseEntity<PhimDTO> updateMovie(@PathVariable Integer id, @RequestBody PhimDTO phimDTO) {
		PhimDTO updatedMovie = phimService.updateMovie(id, phimDTO);
		return ResponseEntity.ok(updatedMovie);
	}

	// #20: Disable movie (soft delete)
	@DeleteMapping("/staff/{id}")
	@PreAuthorize("hasRole('STAFF')")
	public ResponseEntity<Void> disableMovie(@PathVariable Integer id) {
		phimService.disableMovie(id);
		return ResponseEntity.noContent().build();
	}
}
