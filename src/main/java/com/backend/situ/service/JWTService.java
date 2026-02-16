package com.backend.situ.service;

import com.backend.situ.entity.UserCredentials;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration-hours:24}")
    private long expirationHours;

    @Value("${security.jwt.renew-threshold-minutes:15}")
    private long renewThresholdMinutes;

    public String getToken(UserCredentials user) {
        return getToken(new HashMap<>(), user);
    }

    private String getToken(Map<String, Object> extraClaims, UserCredentials userCredentials) {
        long now = System.currentTimeMillis();
        long expiration = now + Duration.ofHours(Math.max(1L, expirationHours)).toMillis();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userCredentials.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException ex) {
            // Permite usar secretos no-base64 en desarrollo local.
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

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String subject = getSubjectFromToken(token);
        return (subject.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    private Date getExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenNearExpiry(String token) {
        Date expiration = getExpiration(token);
        long threshold = Duration.ofMinutes(Math.max(1L, renewThresholdMinutes)).toMillis();
        return expiration.getTime() - System.currentTimeMillis() <= threshold;
    }

    public String renewToken(String token) {
        String email = getSubjectFromToken(token);
        if (email == null || email.isBlank()) {
            return null;
        }

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail(email);
        return getToken(userCredentials);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getKey())
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}
