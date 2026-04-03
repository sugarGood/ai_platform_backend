package com.aiplatform.backend.dto;

import java.time.LocalDateTime;

public record AiUsageEventQuery(
        Long userId,
        Long projectId,
        String sourceType,
        String status,
        LocalDateTime occurredAfter,
        LocalDateTime occurredBefore,
        int page,
        int size
) {
}
