package com.backend.situ.model;

import com.backend.situ.enums.UserRole;

public record UserResponseDTO(
        Long id,
        CompanySummaryDTO company,
        ImageSummaryDTO profileImage,
        Integer dni,
        String firstName,
        String lastName,
        UserRole role
) {
}
