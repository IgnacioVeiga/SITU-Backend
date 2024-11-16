package com.backend.situ.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audits", schema = "public")
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String username;
    private String details;
    private LocalDateTime date;

    public Audit() {}
    public Audit(String action, String username, String details, LocalDateTime date) {
        this.action = action;
        this.username = username;
        this.details = details;
        this.date = date;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}