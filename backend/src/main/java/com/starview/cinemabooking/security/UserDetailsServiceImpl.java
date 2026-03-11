package com.starview.cinemabooking.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.starview.cinemabooking.model.NguoiDung;
import com.starview.cinemabooking.repository.NguoiDungRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private final NguoiDungRepository nguoiDungRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// 1. Fetch the user from your database
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Map your database Role to a Spring Security Authority
        // Note: Spring Security's .hasRole("STAFF") expects the authority to be named "ROLE_STAFF"
        // Adjust "getVaiTro()" to match whatever your role field is named in the NguoiDung entity
        String roleName = "ROLE_" + nguoiDung.getVaiTro().toUpperCase();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        // 3. Return the Spring Security User object
        return new User(
                nguoiDung.getEmail(),
                nguoiDung.getMatKhau(), // This MUST be the Bcrypt hashed password from the DB
                Collections.singletonList(authority)
        );
	}
	

}
