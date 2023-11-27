package com.backend.situ.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "credential", schema = "public")
public class LogInCredentials {
    @Id
    @Column(name = "user_id")
    public int userId;
    public String email;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        // TODO: transform password into a hash or something
        this.password = password;
    }

    public LogInCredentials() { }

    public LogInCredentials(
            String email,
            String password
    ) {
        this.email = email;
        this.setPassword(password);
    }
}
