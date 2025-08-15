package com.carocart.orderservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:36000000}")
    private long jwtExpirationMs;

    @Value("${jwt.internal.ttl-seconds:3600}")
    private long internalTtlSeconds;

    @Value("${jwt.internal.sub:SYSTEM_CALL}")
    private String internalSub;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role, String firstName, String lastName) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateInternalToken() {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(internalTtlSeconds);
        return Jwts.builder()
                .setSubject(internalSub)
                .claim("role", "SYSTEM")
                .claim("userId", 0)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        Object userIdObj = getClaims(token).get("userId");
        if (userIdObj instanceof Number) return ((Number) userIdObj).longValue();
        if (userIdObj instanceof String) {
            try { return Long.parseLong((String) userIdObj); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Claims getClaims(String token) {
        String real = token.startsWith("Bearer ") ? token.substring(7) : token;
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(real).getBody();
    }
}
