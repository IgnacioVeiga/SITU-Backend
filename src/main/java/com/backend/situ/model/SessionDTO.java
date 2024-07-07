package com.backend.situ.model;

import com.backend.situ.enums.UserRole;

public record SessionDTO(
        Long userId,
        Long companyId,
        String logoImageURL,
        String email,
        String fullName,
        UserRole role
) { }
