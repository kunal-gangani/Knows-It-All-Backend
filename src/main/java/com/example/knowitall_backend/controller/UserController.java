package com.example.knowitall_backend.controller;

import com.example.knowitall_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /users/profile/{userId}
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        try {
            Map<String, Object> user = userService.getUserProfile(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // PUT /users/profile/{userId}
    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String userId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            // Ensure user can only update their own profile
            if (!auth.getPrincipal().toString().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false, "error", "Forbidden"));
            }
            String name = body.get("name");
            String email = body.get("email");
            Map<String, Object> updated = userService.updateProfile(userId, name, email);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // PUT /users/location/{userId}
    @PutMapping("/location/{userId}")
    public ResponseEntity<?> updateLocation(
            @PathVariable String userId,
            @RequestBody Map<String, Double> body,
            Authentication auth) {
        try {
            if (!auth.getPrincipal().toString().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false, "error", "Forbidden"));
            }
            Double latitude = body.get("latitude");
            Double longitude = body.get("longitude");
            if (latitude == null || longitude == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "error", "latitude and longitude are required"));
            }
            userService.updateLocation(userId, latitude, longitude);
            return ResponseEntity.ok(Map.of("success", true, "message", "Location updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // GET /users/nearby?latitude=&longitude=&radiusKm=
    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyUsers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            Authentication auth) {
        try {
            String requestingUserId = auth.getPrincipal().toString();
            List<Map<String, Object>> users = userService.getNearbyUsers(
                    requestingUserId, latitude, longitude, radiusKm);
            return ResponseEntity.ok(Map.of("success", true, "data", users));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }

    // POST /users/logout/{userId}
    @PostMapping("/logout/{userId}")
    public ResponseEntity<?> logout(
            @PathVariable String userId,
            Authentication auth) {
        try {
            if (!auth.getPrincipal().toString().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false, "error", "Forbidden"));
            }
            userService.logout(userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Logged out"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "error", e.getMessage()));
        }
    }
}