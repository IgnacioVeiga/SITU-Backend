package com.backend.situ.service;

import com.backend.situ.entity.UserCredentials;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.access-expiration-seconds:900}")
    private long accessExpirationSeconds;

    @PostConstruct
    void validateConfiguration() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("security.jwt.secret must not be empty");
        }
        if (getKey().getEncoded().length < 32) {
            throw new IllegalStateException("security.jwt.secret must be at least 32 bytes");
        }
        if (accessExpirationSeconds <= 0) {
            throw new IllegalStateException("security.jwt.access-expiration-seconds must be positive");
        }
    }

    public String getToken(UserCredentials user) {
        return getToken(new HashMap<>(), user);
    }

    public Instant computeExpirationInstant() {
        return Instant.now().plusSeconds(accessExpirationSeconds);
    }

    private String getToken(Map<String, Object> extraClaims, UserCredentials userCredentials) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessExpirationSeconds);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userCredentials.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException ex) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret key must be at least 32 bytes long");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getSubjectFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    private Date getExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}
