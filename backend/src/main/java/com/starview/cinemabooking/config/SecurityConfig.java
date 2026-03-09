package com.starview.cinemabooking.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.starview.cinemabooking.security.JwtAuthenticationFilter;
import com.starview.cinemabooking.security.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 1. Kích hoạt CORS toàn cục
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                		// Whitelist the Swagger UI and OpenAPI docs so anyone can view them (For Testing, temporary)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                		// Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        // Lưu ý: context-path là /api/v1, nên đường dẫn ở đây là /phim
                        .requestMatchers("/phim/**").permitAll()
                        // Staff endpoints require STAFF role
                        // 2. Tạm thời cho phép tất cả để test, sau này sẽ đổi lại thành .hasRole("STAFF")
                        .requestMatchers("/phim/staff/**").hasRole("STAFF")
                        // Other configuration as needed
                        .anyRequest().authenticated())
        		.addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173")); // Chỉ cho phép React dev server
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Cho phép các phương thức CRUD
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // Cho phép gửi header Authorization và Content-Type
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng cho tất cả các đường dẫn
        return source;
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        // This tells Spring to use BCrypt to check the hashed passwords
        authProvider.setPasswordEncoder(passwordEncoder()); 
        return authProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Industry standard for secure password hashing (US 1.1 AC #11)
        return new BCryptPasswordEncoder(); 
    }
}
