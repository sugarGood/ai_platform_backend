package com.aiplatform.backend.dto;

import jakarta.validation.constraints.Min;

public record CredentialRenewRequest(
        @Min(value = 1, message = "renewDays must be greater than 0")
        Integer renewDays
) {
}
