package com.backend.situ.model;

public record BusLine(
        int id,
        int lineNumber,
        String[] routes
) {
}

