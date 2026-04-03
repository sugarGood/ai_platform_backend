package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EnableProjectKnowledgeConfigRequest(
        @NotNull(message = "kbId must not be null")
        Long kbId,
        BigDecimal searchWeight,
        String injectMode
) {
}
