package com.starview.cinemabooking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.starview.cinemabooking.dtos.NguoiDungDTO;
import com.starview.cinemabooking.mapper.NguoiDungMapper;
import com.starview.cinemabooking.model.NguoiDung;
import com.starview.cinemabooking.repository.NguoiDungRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
	private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
	
	public List<NguoiDungDTO> getAllStaff(){
		return nguoiDungRepository.findByVaiTro("STAFF")
				.stream()
				.map(NguoiDungMapper::toDTO)
				.collect(Collectors.toList());
	}
	
	public void createStaff(NguoiDungDTO nguoiDungDto) {
		// Prevent duplicate emails
        if (nguoiDungRepository.findByEmail(nguoiDungDto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email đã tồn tại trong hệ thống.");
        }

        NguoiDung newStaff = new NguoiDung();
        newStaff.setHoTen(nguoiDungDto.getHoTen());
        newStaff.setEmail(nguoiDungDto.getEmail());
        newStaff.setMatKhau(passwordEncoder.encode(nguoiDungDto.getMatKhau()));
        newStaff.setSoDienThoai(nguoiDungDto.getSoDienThoai());
        newStaff.setNgaySinh(nguoiDungDto.getNgaySinh());
        
        // HARDCODED SECURITY: Even if they somehow pass vaiTro in the request, we ignore it
        newStaff.setVaiTro("STAFF");

        nguoiDungRepository.save(newStaff);
	}
}
