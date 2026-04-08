package com.starview.cinemabooking.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        user.setSoDienThoai(request.getSoDienThoai());
        
        // Bây giờ endpoint này chỉ tạo MEMBER. Tạo tk STAFF chuyển sang AdminController (tạo sau)
        user.setVaiTro("MEMBER"); 
        user.setDiemTichLuy(0); // Khởi tạo 0 điểm
        
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
        extraClaims.put("hoTen", user.getHoTen());
        extraClaims.put("diemTichLuy", user.getDiemTichLuy());

        // 4. Generate the JWT (Satisfies US 1.1 Acceptance Criteria #12)
        String jwtToken = jwtUtils.generateToken(extraClaims, userDetails);

        // 5. Send it back in a clean JSON object
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        
        return ResponseEntity.ok(response);
    }

    // API lấy thông tin profile hiện tại (Để cập nhật điểm Real-time)
    @GetMapping("/my-profile")
    public ResponseEntity<Map<String, Object>> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        NguoiDung user = nguoiDungRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Map<String, Object> response = new HashMap<>();
        response.put("hoTen", user.getHoTen());
        response.put("email", user.getEmail());
        response.put("soDienThoai", user.getSoDienThoai());
        response.put("ngaySinh", user.getNgaySinh());
        response.put("diemTichLuy", user.getDiemTichLuy());
        
        return ResponseEntity.ok(response);
    }

    // API cập nhật thông tin profile
    @PutMapping("/my-profile")
    public ResponseEntity<?> updateMyProfile(@RequestBody NguoiDung updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        NguoiDung user = nguoiDungRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (updateRequest.getHoTen() != null) user.setHoTen(updateRequest.getHoTen());
        if (updateRequest.getSoDienThoai() != null) user.setSoDienThoai(updateRequest.getSoDienThoai());
        if (updateRequest.getNgaySinh() != null) user.setNgaySinh(updateRequest.getNgaySinh());
        if (updateRequest.getMatKhau() != null && !updateRequest.getMatKhau().isEmpty()) {
            user.setMatKhau(passwordEncoder.encode(updateRequest.getMatKhau()));
        }
        
        nguoiDungRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công"));
    }

    // API lấy danh sách tất cả tài khoản để kiểm tra (GET /api/v1/auth/users)
    @GetMapping("/users")
    public ResponseEntity<List<NguoiDung>> getAllUsers() {
        return ResponseEntity.ok(nguoiDungRepository.findAll());
    }
}
