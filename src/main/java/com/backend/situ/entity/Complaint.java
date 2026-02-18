package com.backend.situ.entity;

import com.backend.situ.entity.image.ReportImage;
import com.backend.situ.enums.ComplaintPriority;
import com.backend.situ.enums.ComplaintState;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "complaints", schema = "public")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "reporter_user_id")
    private User reporterUser;

    @ManyToOne
    @JoinColumn(name = "assignee_user_id")
    private User assigneeUser;

    @OneToOne
    @JoinColumn(name = "report_image_id")
    private ReportImage reportImage;

    @Column(name = "description")
    private String description;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::complaint_state")
    @Column(name = "state")
    private ComplaintState state;

    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::complaint_priority")
    @Column(name = "priority")
    private ComplaintPriority priority;

    @Column(name = "is_anonymous")
    private boolean anonymous;

    @Column(name = "contact_email_encrypted")
    private String contactEmailEncrypted;

    @Column(name = "contact_phone_encrypted")
    private String contactPhoneEncrypted;

    @Column(name = "tracking_token")
    private String trackingToken;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "first_response_at")
    private Timestamp firstResponseAt;

    @Column(name = "closed_at")
    private Timestamp closedAt;

    @Column(name = "response_due_at")
    private Timestamp responseDueAt;

    @Column(name = "resolution_due_at")
    private Timestamp resolutionDueAt;

    @ManyToMany
    @JoinTable(
            name = "complaints_lines",
            joinColumns = @JoinColumn(name = "complaint_id"),
            inverseJoinColumns = @JoinColumn(name = "line_id")
    )
    private Set<Line> relatedLines = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "complaints_routes",
            joinColumns = @JoinColumn(name = "complaint_id"),
            inverseJoinColumns = @JoinColumn(name = "route_id")
    )
    private Set<Route> relatedRoutes = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "complaints_stops",
            joinColumns = @JoinColumn(name = "complaint_id"),
            inverseJoinColumns = @JoinColumn(name = "stop_id")
    )
    private Set<Stop> relatedStops = new LinkedHashSet<>();

    public Complaint() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReporterUser() {
        return reporterUser;
    }

    public void setReporterUser(User reporterUser) {
        this.reporterUser = reporterUser;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getAssigneeUser() {
        return assigneeUser;
    }

    public void setAssigneeUser(User assigneeUser) {
        this.assigneeUser = assigneeUser;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ComplaintState getState() {
        return state;
    }

    public void setState(ComplaintState state) {
        this.state = state;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getContactEmailEncrypted() {
        return contactEmailEncrypted;
    }

    public void setContactEmailEncrypted(String contactEmailEncrypted) {
        this.contactEmailEncrypted = contactEmailEncrypted;
    }

    public String getContactPhoneEncrypted() {
        return contactPhoneEncrypted;
    }

    public void setContactPhoneEncrypted(String contactPhoneEncrypted) {
        this.contactPhoneEncrypted = contactPhoneEncrypted;
    }

    public String getTrackingToken() {
        return trackingToken;
    }

    public void setTrackingToken(String trackingToken) {
        this.trackingToken = trackingToken;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getFirstResponseAt() {
        return firstResponseAt;
    }

    public void setFirstResponseAt(Timestamp firstResponseAt) {
        this.firstResponseAt = firstResponseAt;
    }

    public Timestamp getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Timestamp closedAt) {
        this.closedAt = closedAt;
    }

    public Timestamp getResponseDueAt() {
        return responseDueAt;
    }

    public void setResponseDueAt(Timestamp responseDueAt) {
        this.responseDueAt = responseDueAt;
    }

    public Timestamp getResolutionDueAt() {
        return resolutionDueAt;
    }

    public void setResolutionDueAt(Timestamp resolutionDueAt) {
        this.resolutionDueAt = resolutionDueAt;
    }

    public Set<Line> getRelatedLines() {
        return relatedLines;
    }

    public void setRelatedLines(Set<Line> relatedLines) {
        this.relatedLines = relatedLines;
    }

    public Set<Route> getRelatedRoutes() {
        return relatedRoutes;
    }

    public void setRelatedRoutes(Set<Route> relatedRoutes) {
        this.relatedRoutes = relatedRoutes;
    }

    public Set<Stop> getRelatedStops() {
        return relatedStops;
    }

    public void setRelatedStops(Set<Stop> relatedStops) {
        this.relatedStops = relatedStops;
    }
}
