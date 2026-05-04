package com.example.knowitall_backend.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Skip filtering for auth endpoints to prevent unnecessary 403s
        return path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. If no Bearer token, just pass to the next filter (SecurityConfig takes
        // over)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            final String userId = jwtUtil.extractUserId(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!jwtUtil.isTokenExpired(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            new ArrayList<>());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // 2. Catch specific JWT issues (expired, malformed, invalid signature)
            // We log the error but call doFilter so the chain doesn't hang.
            // SecurityConfig will block the request if the route is protected.
            log.error("JWT Validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}