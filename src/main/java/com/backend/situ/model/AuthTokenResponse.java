package com.backend.situ.model;

import java.time.OffsetDateTime;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        OffsetDateTime expiresAt,
        SessionDTO session
) {
}
