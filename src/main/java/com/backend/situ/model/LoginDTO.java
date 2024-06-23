package com.backend.situ.model;

public record LoginDTO(
        String email,
        String password,
        Boolean rememberMe
) {
}
