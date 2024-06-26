package com.backend.situ.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_credentials", schema = "public")
public class UserCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    public int userId;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String encodedPassword;

    public UserCredentials() { }

    public UserCredentials(String email, String encodedPassword) {
        this.email = email;
        this.encodedPassword = encodedPassword;
    }
}
