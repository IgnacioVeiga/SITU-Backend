package com.backend.situ.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class TrackingTokenService {
    private static final int TOKEN_SIZE_BYTES = 32; // 256-bit random token
    private static final int MIN_TOKEN_LENGTH = 24;
    private static final int MAX_TOKEN_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final String hashSecret;

    public TrackingTokenService(@Value("${security.tracking-token.hash-secret:dev-only-change-me}") String hashSecret) {
        this.hashSecret = hashSecret;
    }

    public String generateToken() {
        byte[] random = new byte[TOKEN_SIZE_BYTES];
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public String normalizeToken(String rawToken) {
        if (rawToken == null) {
            return null;
        }

        String token = rawToken.trim();
        if (token.length() < MIN_TOKEN_LENGTH || token.length() > MAX_TOKEN_LENGTH) {
            return null;
        }
        return token;
    }

    public String hashToken(String token) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] digest = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("ERRORS.GENERIC");
        }
    }
}
