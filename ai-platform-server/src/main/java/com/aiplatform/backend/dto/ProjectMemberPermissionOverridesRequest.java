package com.aiplatform.backend.dto;

import jakarta.validation.Valid;
import java.util.List;

public record ProjectMemberPermissionOverridesRequest(
        @Valid List<ProjectMemberPermissionOverrideItemRequest> moduleOverrides,
        List<Long> knowledgeBaseIds,
        List<Long> skillIds,
        List<Long> toolIds,
        List<Long> integrationIds,
        List<Long> atomicCapabilityIds,
        @Valid ProjectMemberQuotaOverrideRequest memberQuota
) {
}
