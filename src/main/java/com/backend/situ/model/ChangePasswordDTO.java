package com.backend.situ.model;

public record ChangePasswordDTO(
    String email,
    String currentPassword,
    String newPassword
){ }
