package com.example.knowitall_backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.knowitall_backend.model.User;
import com.example.knowitall_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get user profile information
     */
    @SuppressWarnings("null")
    public Map<String, Object> getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userToProfileMap(user);
    }

    /**
     * Update user profile (name and email)
     */
    @SuppressWarnings("null")
    public Map<String, Object> updateProfile(String userId, String name, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (name != null && !name.isEmpty()) {
            user.setName(name);
        }
        if (email != null && !email.isEmpty()) {
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(email);
        }

        userRepository.save(user);
        return userToProfileMap(user);
    }

    /**
     * Update user location (latitude and longitude)
     */
    @SuppressWarnings("null")
    public void updateLocation(String userId, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLatitude(latitude);
        user.setLongitude(longitude);

        userRepository.save(user);
    }

    /**
     * Get nearby users within a specified radius
     */

    public List<Map<String, Object>> getNearbyUsers(String requestingUserId,
            Double latitude,
            Double longitude,
            Double radiusKm) {
        // 1 degree of latitude is always ~111.32 km
        double latOffset = radiusKm / 111.32;

        // 1 degree of longitude depends on the latitude: 111.32 * cos(latitude)
        // Convert latitude to radians for the Math.cos function
        double lonOffset = radiusKm / (111.32 * Math.cos(Math.toRadians(latitude)));

        Double latMin = latitude - latOffset;
        Double latMax = latitude + latOffset;
        Double lonMin = longitude - lonOffset;
        Double lonMax = longitude + lonOffset;

        List<User> users = userRepository.findUsersInBoundingBox(
                latMin, latMax, lonMin, lonMax, requestingUserId);

        return users.stream().map(this::userToProfileMap).toList();
    }

    /**
     * Logout user (set online status to false)
     */
    @SuppressWarnings("null")
    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsOnline(false);
        userRepository.save(user);
    }

    /**
     * Helper method to convert User entity to profile map
     */
    private Map<String, Object> userToProfileMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", user.getUid());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("profileImageUrl", user.getProfileImageUrl());
        map.put("latitude", user.getLatitude());
        map.put("longitude", user.getLongitude());
        map.put("skillTokenBalance", user.getSkillTokenBalance());
        map.put("trustScore", user.getTrustScore());
        map.put("profileVerified", user.getProfileVerified());
        map.put("isOnline", user.getIsOnline());
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toEpochMilli() : null);
        map.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toEpochMilli() : null);
        return map;
    }
}
