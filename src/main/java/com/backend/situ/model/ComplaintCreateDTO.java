package com.backend.situ.model;

import com.backend.situ.enums.ComplaintPriority;

import java.util.List;

public record ComplaintCreateDTO(
        String description,
        String reason,
        ComplaintPriority priority,
        Boolean anonymous,
        String contactEmail,
        String contactPhone,
        Long reportImageId,
        List<Long> lineIds,
        List<Long> routeIds,
        List<Long> stopIds
) {
}
