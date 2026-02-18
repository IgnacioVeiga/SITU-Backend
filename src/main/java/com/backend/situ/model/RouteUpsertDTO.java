package com.backend.situ.model;

public record RouteUpsertDTO(
        Long lineId,
        String name,
        String coordinates
) {
}
