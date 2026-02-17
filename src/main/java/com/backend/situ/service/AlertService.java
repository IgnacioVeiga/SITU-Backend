package com.backend.situ.service;

import com.backend.situ.entity.Alert;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.AlertPriority;
import com.backend.situ.enums.AuditAction;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.model.AlertCreateDTO;
import com.backend.situ.model.AlertUpdateDTO;
import com.backend.situ.repository.AlertRepository;
import com.backend.situ.repository.AuthRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AuthRepository authRepository;

    private final ApplicationEventPublisher eventPublisher;

    public AlertService(
            ApplicationEventPublisher eventPublisher,
            AlertRepository alertRepository,
            AuthRepository authRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.alertRepository = alertRepository;
        this.authRepository = authRepository;
    }

    public Page<Alert> listAlerts(int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Timestamp now = Timestamp.from(Instant.now());
        return this.alertRepository.findActiveAt(now, pageable);
    }

    @Transactional
    public Alert createAlert(AlertCreateDTO request, String subjectEmail) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("ERRORS.ALERT.TITLE_REQUIRED");
        }

        if (request.description() == null || request.description().isBlank()) {
            throw new BadRequestException("ERRORS.ALERT.DESCRIPTION_REQUIRED");
        }

        User creator = resolveUserFromSubject(subjectEmail);
        Timestamp now = Timestamp.from(Instant.now());

        Alert alert = new Alert();
        alert.setUser(creator);
        alert.setTitle(request.title().trim());
        alert.setDescription(request.description().trim());
        alert.setLocation(request.location());
        alert.setPriority(request.priority() == null ? AlertPriority.MEDIUM : request.priority());
        alert.setAlertDate(now);
        alert.setStartsAt(request.startsAt() == null ? now : request.startsAt());
        alert.setEndsAt(request.endsAt());
        alert.setActive(true);

        Alert savedAlert = alertRepository.save(alert);
        String details = "New alert by user: " + subjectEmail;
        eventPublisher.publishEvent(new AuditEvent(this, AuditAction.NEW_ALERT, subjectEmail, details));
        return savedAlert;
    }

    @Transactional
    public Alert updateAlert(Long alertId, AlertUpdateDTO request, String subjectEmail) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new BadRequestException("ERRORS.ALERT.NOT_FOUND"));

        if (request.title() != null && !request.title().isBlank()) {
            alert.setTitle(request.title().trim());
        }

        if (request.description() != null && !request.description().isBlank()) {
            alert.setDescription(request.description().trim());
        }

        if (request.location() != null) {
            alert.setLocation(request.location());
        }

        if (request.priority() != null) {
            alert.setPriority(request.priority());
        }

        if (request.startsAt() != null) {
            alert.setStartsAt(request.startsAt());
        }

        if (request.endsAt() != null) {
            alert.setEndsAt(request.endsAt());
        }

        if (request.active() != null) {
            alert.setActive(request.active());
        }

        Alert savedAlert = alertRepository.save(alert);
        String details = "Alert " + alertId + " updated";
        eventPublisher.publishEvent(new AuditEvent(this, AuditAction.ALERT_UPDATED, subjectEmail, details));
        return savedAlert;
    }

    private User resolveUserFromSubject(String subjectEmail) {
        UserCredentials credentials = authRepository.findByEmail(subjectEmail)
                .orElseThrow(() -> new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND"));
        if (credentials.getUser() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }
        return credentials.getUser();
    }
}
