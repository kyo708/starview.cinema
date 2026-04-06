package com.starview.cinemabooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starview.cinemabooking.dtos.NguoiDungDTO;
import com.starview.cinemabooking.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	private final AdminService adminService;
    
    @GetMapping("/staff")
    public ResponseEntity<List<NguoiDungDTO>> getAllStaff(){
		return ResponseEntity.ok(adminService.getAllStaff());
    	
    }
    
    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody NguoiDungDTO request) {
    	try {
    		adminService.createStaff(request);
    		return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Tạo tài khoản nhân viên thành công."));
    		
    	} catch (IllegalStateException e) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email đã tồn tại trong hệ thống."));
    	}
    	
    }
}
