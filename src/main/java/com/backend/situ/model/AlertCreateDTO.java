package com.backend.situ.model;

import com.backend.situ.enums.AlertPriority;

import java.sql.Timestamp;

public record AlertCreateDTO(
        String title,
        String description,
        String location,
        AlertPriority priority,
        Timestamp startsAt,
        Timestamp endsAt
) {
}
