package com.backend.situ.service;

import com.backend.situ.entity.Complaint;
import com.backend.situ.entity.Line;
import com.backend.situ.entity.Route;
import com.backend.situ.entity.Stop;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.entity.image.ReportImage;
import com.backend.situ.enums.AuditAction;
import com.backend.situ.enums.ComplaintPriority;
import com.backend.situ.enums.ComplaintState;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.model.ComplaintAssignDTO;
import com.backend.situ.model.ComplaintCreateDTO;
import com.backend.situ.model.ComplaintResponseDTO;
import com.backend.situ.model.ComplaintStateUpdateDTO;
import com.backend.situ.model.ComplaintUserSummaryDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.ComplaintRepository;
import com.backend.situ.repository.LineRepository;
import com.backend.situ.repository.RouteRepository;
import com.backend.situ.repository.StopRepository;
import com.backend.situ.repository.UserRepository;
import com.backend.situ.repository.image.ReportImageRepository;
import com.backend.situ.util.ContactMaskingUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ApplicationEventPublisher eventPublisher;
    private final ComplaintRepository complaintRepository;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final LineRepository lineRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final ReportImageRepository reportImageRepository;
    private final SensitiveDataService sensitiveDataService;
    private final ComplaintNotificationService complaintNotificationService;

    public ComplaintService(
            ApplicationEventPublisher eventPublisher,
            ComplaintRepository complaintRepository,
            AuthRepository authRepository,
            UserRepository userRepository,
            LineRepository lineRepository,
            RouteRepository routeRepository,
            StopRepository stopRepository,
            ReportImageRepository reportImageRepository,
            SensitiveDataService sensitiveDataService,
            ComplaintNotificationService complaintNotificationService
    ) {
        this.eventPublisher = eventPublisher;
        this.complaintRepository = complaintRepository;
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.lineRepository = lineRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.reportImageRepository = reportImageRepository;
        this.sensitiveDataService = sensitiveDataService;
        this.complaintNotificationService = complaintNotificationService;
    }

    public Page<ComplaintResponseDTO> listComplaints(int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return complaintRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponseDTO);
    }

    public Page<ComplaintResponseDTO> listMyComplaints(String subjectEmail, int pageIndex, int pageSize) {
        User reporter = resolveUserFromSubject(subjectEmail);
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return complaintRepository
                .findByReporterUserIdOrderByCreatedAtDesc(reporter.getId(), pageable)
                .map(this::toResponseDTO);
    }

    public ComplaintResponseDTO getComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BadRequestException("ERRORS.COMPLAINT.NOT_FOUND"));
        return toResponseDTO(complaint);
    }

    public ComplaintResponseDTO getComplaintByTrackingToken(String trackingToken) {
        Complaint complaint = complaintRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new BadRequestException("ERRORS.COMPLAINT.NOT_FOUND"));
        return toResponseDTO(complaint);
    }

    @Transactional
    public ComplaintResponseDTO createComplaint(ComplaintCreateDTO request, String subjectEmail) {
        if (request == null || request.description() == null || request.description().isBlank()) {
            throw new BadRequestException("ERRORS.COMPLAINT.DESCRIPTION_REQUIRED");
        }

        User reporter = resolveUserFromSubject(subjectEmail);
        ComplaintPriority priority = request.priority() == null ? ComplaintPriority.MEDIUM : request.priority();
        Timestamp now = Timestamp.from(Instant.now());

        Complaint complaint = new Complaint();
        complaint.setReporterUser(reporter);
        complaint.setDescription(request.description().trim());
        complaint.setReason(request.reason());
        complaint.setPriority(priority);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setAnonymous(Boolean.TRUE.equals(request.anonymous()));
        complaint.setTrackingToken(generateTrackingToken());
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);
        complaint.setResponseDueAt(Timestamp.from(resolveResponseSla(now.toInstant(), priority)));
        complaint.setResolutionDueAt(Timestamp.from(resolveResolutionSla(now.toInstant(), priority)));
        complaint.setContactEmailEncrypted(sensitiveDataService.encrypt(request.contactEmail()));
        complaint.setContactPhoneEncrypted(sensitiveDataService.encrypt(request.contactPhone()));
        complaint.setRelatedLines(resolveLines(request.lineIds()));
        complaint.setRelatedRoutes(resolveRoutes(request.routeIds()));
        complaint.setRelatedStops(resolveStops(request.stopIds()));

        if (request.reportImageId() != null) {
            ReportImage reportImage = reportImageRepository.findById(request.reportImageId())
                    .orElseThrow(() -> new BadRequestException("ERRORS.COMPLAINT.IMAGE_NOT_FOUND"));
            complaint.setReportImage(reportImage);
        }

        Complaint storedComplaint = complaintRepository.save(complaint);

        String details = "Complaint created by: " + reporter.getId();
        eventPublisher.publishEvent(new AuditEvent(this, AuditAction.NEW_COMPLAINT, subjectEmail, details));

        return toResponseDTO(storedComplaint);
    }

    @Transactional
    public ComplaintResponseDTO updateComplaintState(
            Long complaintId,
            ComplaintStateUpdateDTO request,
            String subjectEmail
    ) {
        if (request == null || request.state() == null) {
            throw new BadRequestException("ERRORS.COMPLAINT.STATE_REQUIRED");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BadRequestException("ERRORS.COMPLAINT.NOT_FOUND"));

        ComplaintState currentState = complaint.getState();
        ComplaintState targetState = request.state();
        validateStateTransition(currentState, targetState);

        Timestamp now = Timestamp.from(Instant.now());
        complaint.setState(targetState);
        complaint.setUpdatedAt(now);

        if (targetState == ComplaintState.IN_REVIEW && complaint.getFirstResponseAt() == null) {
            complaint.setFirstResponseAt(now);
        }

        if (targetState == ComplaintState.CLOSED) {
            complaint.setClosedAt(now);
        } else if (targetState == ComplaintState.REOPENED) {
            complaint.setClosedAt(null);
        }

        if (complaint.getAssigneeUser() == null) {
            complaint.setAssigneeUser(resolveUserFromSubject(subjectEmail));
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        String details = "Complaint " + complaintId + " moved from " + currentState + " to " + targetState;
        eventPublisher.publishEvent(new AuditEvent(this, AuditAction.COMPLAINT_STATUS_UPDATED, subjectEmail, details));

        complaintNotificationService.notifyStatusChanged(savedComplaint);
        return toResponseDTO(savedComplaint);
    }

    @Transactional
    public ComplaintResponseDTO assignComplaint(
            Long complaintId,
            ComplaintAssignDTO request,
            String subjectEmail
    ) {
        if (request == null || request.assigneeUserId() == null) {
            throw new BadRequestException("ERRORS.COMPLAINT.ASSIGNEE_REQUIRED");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BadRequestException("ERRORS.COMPLAINT.NOT_FOUND"));

        User assignee = userRepository.findById(request.assigneeUserId())
                .orElseThrow(() -> new BadRequestException("ERRORS.COMPLAINT.ASSIGNEE_NOT_FOUND"));

        complaint.setAssigneeUser(assignee);
        complaint.setUpdatedAt(Timestamp.from(Instant.now()));
        Complaint savedComplaint = complaintRepository.save(complaint);

        String details = "Complaint " + complaintId + " assigned to user " + assignee.getId();
        eventPublisher.publishEvent(new AuditEvent(this, AuditAction.COMPLAINT_ASSIGNED, subjectEmail, details));

        return toResponseDTO(savedComplaint);
    }

    private User resolveUserFromSubject(String subjectEmail) {
        UserCredentials credentials = authRepository.findByEmail(subjectEmail)
                .orElseThrow(() -> new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND"));
        if (credentials.getUser() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }
        return credentials.getUser();
    }

    private Set<Line> resolveLines(List<Long> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) {
            return Set.of();
        }
        return lineRepository.findAllById(lineIds).stream().collect(Collectors.toSet());
    }

    private Set<Route> resolveRoutes(List<Long> routeIds) {
        if (routeIds == null || routeIds.isEmpty()) {
            return Set.of();
        }
        return routeRepository.findAllById(routeIds).stream().collect(Collectors.toSet());
    }

    private Set<Stop> resolveStops(List<Long> stopIds) {
        if (stopIds == null || stopIds.isEmpty()) {
            return Set.of();
        }
        return stopRepository.findAllById(stopIds).stream().collect(Collectors.toSet());
    }

    private String generateTrackingToken() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private Instant resolveResponseSla(Instant createdAt, ComplaintPriority priority) {
        // SLA can be tuned in one place without changing business flow code.
        return switch (priority) {
            case HIGH -> createdAt.plus(24, ChronoUnit.HOURS);
            case MEDIUM, LOW -> createdAt.plus(72, ChronoUnit.HOURS);
        };
    }

    private Instant resolveResolutionSla(Instant createdAt, ComplaintPriority priority) {
        // Initial SLA policy from product decision:
        // high priority: 7 days, others: 15 days.
        return switch (priority) {
            case HIGH -> createdAt.plus(7, ChronoUnit.DAYS);
            case MEDIUM, LOW -> createdAt.plus(15, ChronoUnit.DAYS);
        };
    }

    private void validateStateTransition(ComplaintState currentState, ComplaintState targetState) {
        if (currentState == null || targetState == null || currentState == targetState) {
            return;
        }

        boolean allowed = switch (currentState) {
            case PENDING_REVIEW -> targetState == ComplaintState.IN_REVIEW || targetState == ComplaintState.CLOSED;
            case IN_REVIEW -> targetState == ComplaintState.CLOSED || targetState == ComplaintState.REOPENED;
            case CLOSED -> targetState == ComplaintState.REOPENED;
            case REOPENED -> targetState == ComplaintState.IN_REVIEW || targetState == ComplaintState.CLOSED;
        };

        if (!allowed) {
            throw new BadRequestException("ERRORS.COMPLAINT.INVALID_STATE_TRANSITION");
        }
    }

    private ComplaintResponseDTO toResponseDTO(Complaint complaint) {
        String email = sensitiveDataService.decrypt(complaint.getContactEmailEncrypted());
        String phone = sensitiveDataService.decrypt(complaint.getContactPhoneEncrypted());

        ComplaintUserSummaryDTO reporter = null;
        if (complaint.getReporterUser() != null) {
            reporter = new ComplaintUserSummaryDTO(
                    complaint.getReporterUser().getId(),
                    complaint.getReporterUser().getFirstName() + " " + complaint.getReporterUser().getLastName()
            );
        }

        ComplaintUserSummaryDTO assignee = null;
        if (complaint.getAssigneeUser() != null) {
            assignee = new ComplaintUserSummaryDTO(
                    complaint.getAssigneeUser().getId(),
                    complaint.getAssigneeUser().getFirstName() + " " + complaint.getAssigneeUser().getLastName()
            );
        }

        Long imageId = complaint.getReportImage() == null ? null : complaint.getReportImage().getId();

        return new ComplaintResponseDTO(
                complaint.getId(),
                reporter,
                assignee,
                complaint.getDescription(),
                complaint.getReason(),
                complaint.getState(),
                complaint.getPriority(),
                complaint.isAnonymous(),
                ContactMaskingUtils.maskEmail(email),
                ContactMaskingUtils.maskPhone(phone),
                complaint.getTrackingToken(),
                imageId,
                complaint.getCreatedAt(),
                complaint.getUpdatedAt(),
                complaint.getFirstResponseAt(),
                complaint.getClosedAt(),
                complaint.getResponseDueAt(),
                complaint.getResolutionDueAt(),
                complaint.getRelatedLines().stream().map(Line::getId).collect(Collectors.toSet()),
                complaint.getRelatedRoutes().stream().map(Route::getId).collect(Collectors.toSet()),
                complaint.getRelatedStops().stream().map(Stop::getId).collect(Collectors.toSet())
        );
    }
}
