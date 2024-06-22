package com.backend.situ.model;

public record LogInFrom(
        String email,
        String password,
        Boolean rememberMe
) {
}
