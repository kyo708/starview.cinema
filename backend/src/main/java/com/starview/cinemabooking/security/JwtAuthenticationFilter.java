package com.starview.cinemabooking.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Look for the "Authorization" header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. If there's no token, or it doesn't start with "Bearer ", move on to the next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token string (skip the first 7 characters "Bearer ")
        jwt = authHeader.substring(7);
        
        // 4. Extract the email from the token
        userEmail = jwtUtils.extractUsername(jwt);

        // 5. If we have an email AND the user isn't already authenticated in this context...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Load the user details from our database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Validate the token mathematically
            if (jwtUtils.isTokenValid(jwt, userDetails)) {
                
                // Create the authentication object Spring Security needs
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                // Attach details about the current web request
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Update the Security Context to say "This user is officially logged in!"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 7. Continue processing the request
        filterChain.doFilter(request, response);
    }
}
