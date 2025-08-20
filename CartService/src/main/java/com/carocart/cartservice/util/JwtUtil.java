package com.carocart.cartservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    private static final long JWT_EXPIRATION_MS = 1000 * 60 * 60 * 10; // 10 hours

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ----------------- Token Generation -----------------
    public String generateToken(Long userId, String email, String role, String firstName, String lastName) {
        return Jwts.builder()
            .setSubject(email)
            .claim("userId", userId)
            .claim("role", role)
            .claim("firstName", firstName)
            .claim("lastName", lastName)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    // ----------------- Validation -----------------
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(cleanToken(token));
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    // ----------------- Extractors -----------------
    public String extractUsername(String token) {
        return extractAllClaims(cleanToken(token)).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(cleanToken(token)).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(cleanToken(token)).get("userId", Long.class);
    }

    public String extractFirstName(String token) {
        return extractAllClaims(cleanToken(token)).get("firstName", String.class);
    }

    public String extractLastName(String token) {
        return extractAllClaims(cleanToken(token)).get("lastName", String.class);
    }

    // ----------------- Private Helpers -----------------
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    // Clean "Bearer " prefix if present
    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
