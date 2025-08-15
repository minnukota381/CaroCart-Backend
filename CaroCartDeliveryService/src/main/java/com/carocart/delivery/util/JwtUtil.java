package com.carocart.delivery.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.internal.ttl-seconds:3600}")
    private long internalTtlSeconds;

    @Value("${jwt.internal.sub:SYSTEM_CALL}")
    private String internalSub;

    private SecretKey key;

    private final AtomicReference<CachedToken> cachedInternal = new AtomicReference<>();

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Long userId, String role, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("name", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Internal SYSTEM token (cached)
    public String generateInternalToken() {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(internalTtlSeconds);
        Map<String, Object> claims = Map.of("role", "SYSTEM", "userId", 0L);

        return Jwts.builder()
                .setSubject(internalSub)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getOrGenerateInternalToken() {
        Instant now = Instant.now();
        CachedToken ct = cachedInternal.get();
        if (ct != null && ct.expiresAt > now.plusSeconds(60).getEpochSecond()) {
            return ct.value;
        }
        String token = generateInternalToken();
        Claims c = getClaims(token);
        cachedInternal.set(new CachedToken(token, c.getExpiration().toInstant().getEpochSecond()));
        return token;
    }

    private static class CachedToken {
        final String value;
        final long expiresAt;
        CachedToken(String v, long e) { this.value = v; this.expiresAt = e; }
    }

    public Claims getClaims(String token) {
        String real = token.startsWith("Bearer ") ? token.substring(7) : token;
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(real).getBody();
    }
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            return getClaims(token).getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        Object userIdObj = getClaims(token).get("userId");
        if (userIdObj instanceof Number) return ((Number) userIdObj).longValue();
        if (userIdObj instanceof String) return Long.parseLong((String) userIdObj);
        return null;
    }

    public Long extractUserId(Claims claims) {
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) return ((Number) userIdObj).longValue();
        if (userIdObj instanceof String) return Long.parseLong((String) userIdObj);
        return null;
    }
}
