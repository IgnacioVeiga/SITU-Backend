package com.backend.situ.model;

public record SignUpForm(
    String companyName,
    String email,
    String phone,
    String firstName,
    String lastName,
    int dni,
    String notes
) { }
