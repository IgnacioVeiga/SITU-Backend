package com.backend.situ.entity;

import com.backend.situ.entity.image.ReportImage;
import com.backend.situ.enums.ReportState;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "reports", schema = "public")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "report_image_id")
    private ReportImage reportImage;

    @Column(name = "description")
    private String description;

    @Column(name = "report_date")
    private Timestamp reportDate;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private ReportState state;

    public Report() {
    }

    public Report(Long id, User user, ReportImage reportImage, String description, Timestamp reportDate, String reason, ReportState state) {
        this.id = id;
        this.user = user;
        this.reportImage = reportImage;
        this.description = description;
        this.reportDate = reportDate;
        this.reason = reason;
        this.state = state;
    }

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

    public ReportImage getReportImage() {
        return reportImage;
    }

    public void setReportImage(ReportImage reportImage) {
        this.reportImage = reportImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getReportDate() {
        return reportDate;
    }

    public void setReportDate(Timestamp reportDate) {
        this.reportDate = reportDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReportState getState() {
        return state;
    }

    public void setState(ReportState state) {
        this.state = state;
    }
}
