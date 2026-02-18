package com.backend.situ.component;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.Audit;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.AuditRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditEventListener {
    private final AuditRepository auditRepository;
    private final AuthRepository authRepository;

    public AuditEventListener(AuditRepository auditRepository, AuthRepository authRepository) {
        this.auditRepository = auditRepository;
        this.authRepository = authRepository;
    }

    @EventListener
    public void onAuditEvent(AuditEvent event) {
        Company company = null;
        if (event.getUsername() != null && !event.getUsername().isBlank()) {
            company = authRepository.findByEmail(event.getUsername())
                    .map(credentials -> credentials.getUser() != null ? credentials.getUser().getCompany() : null)
                    .orElse(null);
        }

        Audit record = new Audit(
                event.getAction(),
                company,
                event.getUsername(),
                event.getDetails(),
                LocalDateTime.now()
        );
        auditRepository.save(record);
    }
}
