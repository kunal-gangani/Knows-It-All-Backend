package com.example.knowitall_backend.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.example.knowitall_backend.config.JwtProperties; // Import your properties

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // 1. Declare as final and use the Properties class
    private final JwtProperties jwtProperties;

    // 2. Inject via Constructor
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // ── Token generation ──────────────────────────────────────────────────────

    public String generateToken(String userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        return buildToken(claims, userId);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        var now = new Date();
        // 3. Use jwtProperties.getExpiration()
        var expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        var builder = Jwts.builder();

        for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
            builder = builder.claim(entry.getKey(), entry.getValue());
        }

        return builder
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token validation ──────────────────────────────────────────────────────

    public boolean isTokenValid(String token, String userId) {
        final String extractedUserId = extractUserId(token);
        return extractedUserId.equals(userId) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claims extraction ─────────────────────────────────────────────────────

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        // 4. Use jwtProperties.getSecret()
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}