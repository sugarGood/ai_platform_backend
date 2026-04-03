package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePromptPriorityRequest(
        @NotNull(message = "priority is required")
        Integer priority
) {
}
