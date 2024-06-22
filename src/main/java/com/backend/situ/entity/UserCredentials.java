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
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserCredentials() { }

    public UserCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
