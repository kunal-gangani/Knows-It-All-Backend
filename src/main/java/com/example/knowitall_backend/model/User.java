package com.example.knowitall_backend.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String uid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    @Builder.Default
    private Double latitude = 0.0;

    @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    @Builder.Default
    private Double longitude = 0.0;

    @Column(name = "skill_token_balance", columnDefinition = "BIGINT DEFAULT 100")
    @Builder.Default
    private Long skillTokenBalance = 100L;

    @Column(name = "trust_score", columnDefinition = "FLOAT DEFAULT 0.0")
    @Builder.Default
    private Float trustScore = 0.0f;

    @Column(name = "profile_verified", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean profileVerified = false;

    @Column(name = "is_online", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isOnline = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}