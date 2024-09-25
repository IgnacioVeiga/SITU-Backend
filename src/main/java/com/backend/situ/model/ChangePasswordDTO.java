package com.backend.situ.model;

public record ChangePasswordDTO(
    String currentPassword,
    String newPassword
){ }
