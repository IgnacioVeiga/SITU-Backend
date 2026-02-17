package com.backend.situ.model;

import com.backend.situ.enums.ComplaintState;

public record ComplaintStateUpdateDTO(
        ComplaintState state
) {
}
