package com.aiplatform.backend.dto;

import java.util.List;

public record ProjectMemberScopedResourceSummaryResponse(
        String accessLevel,
        String scopeMode,
        int totalCount,
        int accessibleCount,
        List<Long> selectedResourceIds,
        List<String> selectedResourceNames
) {
}
