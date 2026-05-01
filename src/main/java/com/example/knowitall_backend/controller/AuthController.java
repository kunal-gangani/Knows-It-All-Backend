package com.example.knowitall_backend.controller;

import com.example.knowitall_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String email = body.get("email");
            String password = body.get("password");

            if (name == null || email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "name, email and password are required"));
            }
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Password must be at least 6 characters"));
            }

            Map<String, Object> authData = authService.register(name, email, password);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", authData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    // POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "email and password are required"));
            }

            Map<String, Object> authData = authService.login(email, password);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", authData));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }
}