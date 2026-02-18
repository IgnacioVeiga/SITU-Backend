package com.backend.situ.model;

public record StopUpsertDTO(
        String name,
        String locationGeoJson
) {
}
