package com.backend.situ.component;

import com.backend.situ.entity.Audit;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.repository.AuditRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditEventListener {
    private final AuditRepository auditRepository;

    public AuditEventListener(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @EventListener
    public void onAuditEvent(AuditEvent event) {
        Audit record = new Audit(
                event.getAction(),
                event.getUsername(),
                event.getDetails(),
                LocalDateTime.now()
        );
        auditRepository.save(record);
    }
}
