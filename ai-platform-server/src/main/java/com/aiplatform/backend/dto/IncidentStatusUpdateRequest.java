package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record IncidentStatusUpdateRequest(
        @NotBlank(message = "Status must not be blank")
        String status
) {
}
