package com.backend.situ.service;

import com.backend.situ.entity.Alert;
import com.backend.situ.enums.AuditAction;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    @Autowired
    private final AlertRepository alertRepository;

    private final ApplicationEventPublisher eventPublisher;
    
    @Autowired
    public AlertService(ApplicationEventPublisher eventPublisher, AlertRepository alertRepository) {
        this.eventPublisher = eventPublisher;
        this.alertRepository = alertRepository;
    }

    public Page<Alert> listAlerts(int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return this.alertRepository.findAll(pageable);
    }

    public Alert createAlert(Alert alert) {
        String username = alert.getUser().getLastName() + alert.getUser().getFirstName();
        String details = "New alert by user: " + username;
        AuditEvent auditEvent = new AuditEvent(this, AuditAction.NEW_ALERT, username, details);
        eventPublisher.publishEvent(auditEvent);
        
        return this.alertRepository.save(alert);
    }
}
