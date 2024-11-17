package com.backend.situ.event;

import com.backend.situ.enums.AuditAction;
import org.springframework.context.ApplicationEvent;

public class AuditEvent extends ApplicationEvent {
    private final AuditAction action;
    private final String username;
    private final String details;

    public AuditEvent(Object source, AuditAction action, String username, String details) {
        super(source);
        this.action = action;
        this.username = username;
        this.details = details;
    }

    public AuditAction getAction() { return action; }
    public String getUsername() { return username; }
    public String getDetails() { return details; }
}
