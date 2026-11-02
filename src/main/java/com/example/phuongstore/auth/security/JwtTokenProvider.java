package com.example.phuongstore.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long defaultExpirationMs;
    private final long rememberMeExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:phuongstore_super_secure_jwt_secret_key_2026_at_least_256_bits_long!!}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long defaultExpirationMs,
            @Value("${jwt.remember-me-expiration-ms:604800000}") long rememberMeExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.defaultExpirationMs = defaultExpirationMs;
        this.rememberMeExpirationMs = rememberMeExpirationMs;
    }

    public String generateToken(String username, boolean rememberMe) {
        long duration = rememberMe ? rememberMeExpirationMs : defaultExpirationMs;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + duration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public long getExpirationDuration(boolean rememberMe) {
        return rememberMe ? rememberMeExpirationMs : defaultExpirationMs;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

