package com.backend.situ.model;

import com.backend.situ.enums.UserRole;

public record UserUpdateDTO(
        Integer dni,
        String firstName,
        String lastName,
        UserRole role
) {
}
