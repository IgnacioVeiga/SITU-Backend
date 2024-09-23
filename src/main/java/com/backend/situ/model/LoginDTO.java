package com.backend.situ.model;

public record LoginDTO(
        String email,
        String password,
        Boolean rememberMe // TODO: revisar si es necesario para el backend
) { }
