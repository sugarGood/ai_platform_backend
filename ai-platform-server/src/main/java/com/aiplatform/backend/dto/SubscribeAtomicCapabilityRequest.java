package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

public record SubscribeAtomicCapabilityRequest(
        @NotNull(message = "capabilityId is required")
        Long capabilityId
) {
}
