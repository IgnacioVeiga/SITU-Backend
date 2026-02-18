package com.backend.situ.model;

public record LineResponseDTO(
        Long id,
        String number,
        String name,
        CompanySummaryDTO company
) {
}
