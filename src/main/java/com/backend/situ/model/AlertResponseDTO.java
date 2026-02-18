package com.backend.situ.model;

import com.backend.situ.enums.AlertPriority;

import java.sql.Timestamp;

public record AlertResponseDTO(
        Long id,
        AlertUserSummaryDTO user,
        String title,
        String description,
        Timestamp alertDate,
        Timestamp startsAt,
        Timestamp endsAt,
        Boolean active,
        AlertPriority priority,
        String location
) {
}
