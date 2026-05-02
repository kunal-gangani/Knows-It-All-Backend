package com.example.knowitall_backend.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.knowitall_backend.model.User;
import com.example.knowitall_backend.repository.UserRepository;
import com.example.knowitall_backend.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ── Register ──────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    public Map<String, Object> register(String name, String email, String password) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .uid(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .skillTokenBalance(100L) // starting balance
                .trustScore(0.0f)
                .profileVerified(false)
                .isOnline(true)
                .latitude(0.0)
                .longitude(0.0)
                .build();

        userRepository.save(user);

        // Generate JWT
        String token = jwtUtil.generateToken(user.getUid(), user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getUid());
        response.put("name", user.getName());
        return response;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    public Map<String, Object> login(String email, String password) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Mark user as online
        user.setIsOnline(true);
        userRepository.save(user);

        // Generate JWT
        String token = jwtUtil.generateToken(user.getUid(), user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getUid());
        response.put("name", user.getName());
        return response;
    }
}