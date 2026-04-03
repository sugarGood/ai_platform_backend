package com.aiplatform.backend.dto;

import jakarta.validation.constraints.Min;

public record CredentialRotateRequest(
        @Min(value = 1, message = "gracePeriodHours must be greater than 0")
        Integer gracePeriodHours
) {
}
