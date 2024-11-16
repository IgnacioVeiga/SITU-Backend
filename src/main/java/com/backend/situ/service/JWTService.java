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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private final long JWT_EXP_TIME = 1000 * 60 * 24; // 24 hs
    private final long RENEW_THRESHOLD = 1000 * 60 * 15; // 15 min

    public String getToken(UserCredentials user) {
        return getToken(new HashMap<>(), user);
    }

    private String getToken(Map<String, Object> extraClaims, UserCredentials userCredentials) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userCredentials.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXP_TIME))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
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
        return expiration.getTime() - System.currentTimeMillis() <= RENEW_THRESHOLD;
    }

    public String renewToken(String token) {
        String email = getSubjectFromToken(token);
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail(email);
        return getToken(userCredentials);
    }

    private Claims extractAllClaims(String token)
    {
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
