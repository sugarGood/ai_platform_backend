package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillFeedbackRequest(
        @NotBlank(message = "rating must not be blank")
        String rating
) {
}
