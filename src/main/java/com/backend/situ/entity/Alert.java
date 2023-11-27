package com.backend.situ.entity;

import java.sql.Timestamp;

import com.backend.situ.enums.AlertPriority;
import jakarta.persistence.*;

@Entity
@Table(name = "alerts", schema = "public")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "alert_date")
    private Timestamp alertDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private AlertPriority priority;

    @Column(name = "location")
    private String location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getAlertDate() {
        return alertDate;
    }

    public void setAlertDate(Timestamp alertDate) {
        this.alertDate = alertDate;
    }

    public AlertPriority getPriority() {
        return priority;
    }

    public void setPriority(AlertPriority priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Alert() {
    }

    public Alert(Long id, User user, String title, String description, Timestamp alertDate, AlertPriority priority, String location) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.alertDate = alertDate;
        this.priority = priority;
        this.location = location;
    }
}
