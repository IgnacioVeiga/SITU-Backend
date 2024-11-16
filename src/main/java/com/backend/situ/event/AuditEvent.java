package com.backend.situ.event;

import org.springframework.context.ApplicationEvent;

public class AuditEvent extends ApplicationEvent {
    private final String action;
    private final String username;
    private final String details;

    public AuditEvent(Object source, String action, String username, String details) {
        super(source);
        this.action = action;
        this.username = username;
        this.details = details;
    }

    public String getAction() { return action; }
    public String getUsername() { return username; }
    public String getDetails() { return details; }
}
