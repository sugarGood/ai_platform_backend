package com.aiplatform.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record KnowledgeBaseSearchRequest(
        @NotBlank(message = "query must not be blank")
        String query,
        @Min(value = 1, message = "resultCount must be greater than 0")
        Integer resultCount,
        Double scoreThreshold
) {
}
