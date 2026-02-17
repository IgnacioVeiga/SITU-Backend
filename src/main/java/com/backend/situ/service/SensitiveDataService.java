package com.backend.situ.service;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class SensitiveDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveDataService.class);

    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec secretKey;

    public SensitiveDataService(@Value("${security.data.encryption-key:dev-only-change-me}") String encryptionKey) {
        this.secretKey = new SecretKeySpec(deriveAes256Key(encryptionKey), "AES");
    }

    public String encrypt(String plainValue) {
        if (plainValue == null || plainValue.isBlank()) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(AES_GCM_TAG_LENGTH_BITS, iv));

            byte[] cipherText = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(cipherText, 0, payload, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception ex) {
            LOGGER.error("Sensitive data encryption failed", ex);
            throw new IllegalStateException("ERRORS.GENERIC");
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return null;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(encryptedValue);
            if (payload.length <= GCM_IV_LENGTH_BYTES) {
                return null;
            }

            // The payload stores [12-byte IV][cipher text + auth tag].
            byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_LENGTH_BYTES);
            byte[] cipherBytes = Arrays.copyOfRange(payload, GCM_IV_LENGTH_BYTES, payload.length);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(AES_GCM_TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            LOGGER.error("Sensitive data decryption failed", ex);
            return null;
        }
    }

    private byte[] deriveAes256Key(@Nonnull String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            LOGGER.error("Could not derive encryption key", ex);
            throw new IllegalStateException("ERRORS.GENERIC");
        }
    }
}
