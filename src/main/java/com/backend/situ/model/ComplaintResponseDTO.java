package com.backend.situ.model;

import com.backend.situ.enums.ComplaintPriority;
import com.backend.situ.enums.ComplaintState;

import java.sql.Timestamp;
import java.util.Set;

public record ComplaintResponseDTO(
        Long id,
        ComplaintUserSummaryDTO reporter,
        ComplaintUserSummaryDTO assignee,
        String description,
        String reason,
        ComplaintState state,
        ComplaintPriority priority,
        boolean anonymous,
        String maskedContactEmail,
        String maskedContactPhone,
        String trackingToken,
        Long reportImageId,
        Timestamp createdAt,
        Timestamp updatedAt,
        Timestamp firstResponseAt,
        Timestamp closedAt,
        Timestamp responseDueAt,
        Timestamp resolutionDueAt,
        Set<Long> lineIds,
        Set<Long> routeIds,
        Set<Long> stopIds
) {
}
