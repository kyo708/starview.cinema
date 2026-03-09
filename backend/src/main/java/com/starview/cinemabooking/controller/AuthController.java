package com.starview.cinemabooking.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.starview.cinemabooking.model.NguoiDung;
import com.starview.cinemabooking.repository.NguoiDungRepository;
import com.starview.cinemabooking.security.JwtUtils;
import com.starview.cinemabooking.security.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    // Register Endpoint
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody NguoiDung request) {
        if (nguoiDungRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        // Lưu hash mật khẩu (US 1.1 #11)
        NguoiDung user = new NguoiDung();
        user.setHoTen(request.getHoTen());
        user.setEmail(request.getEmail());
        user.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        user.setVaiTro(request.getVaiTro() != null ? request.getVaiTro() : "CUSTOMER");
        user.setSoDienThoai(request.getSoDienThoai());

        nguoiDungRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("matKhau");

        // 1. Authenticate the user (Checks if email and BCrypt password match)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // 2. Fetch the user details and the actual DB record
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        NguoiDung user = nguoiDungRepository.findByEmail(email).get();

        // 3. Bake the "Extra Claims" into the token so the frontend doesn't have to ask for them later
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getVaiTro());
        extraClaims.put("userId", user.getId());
        extraClaims.put("fullName", user.getHoTen());

        // 4. Generate the JWT (Satisfies US 1.1 Acceptance Criteria #12)
        String jwtToken = jwtUtils.generateToken(extraClaims, userDetails);

        // 5. Send it back in a clean JSON object
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        
        return ResponseEntity.ok(response);
    }
}
