package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

public record SkillReviewRequest(
        @NotNull(message = "approved must not be null")
        Boolean approved,
        String comment
) {
}
