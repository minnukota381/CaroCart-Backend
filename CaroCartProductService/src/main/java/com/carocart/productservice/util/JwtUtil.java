package com.carocart.productservice.util;

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
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.internal.ttl-seconds:3600}") private long ttlSeconds;
    @Value("${jwt.internal.sub:SYSTEM_CALL}") private String internalSub;
    private Key key;

    @PostConstruct public void init() { this.key = Keys.hmacShaKeyFor(secret.getBytes()); }

    public String generateInternalBearer() {
        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .setSubject(internalSub)
                .claim("role", "SYSTEM")
                .claim("userId", 0)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return "Bearer " + jwt;
    }
}
