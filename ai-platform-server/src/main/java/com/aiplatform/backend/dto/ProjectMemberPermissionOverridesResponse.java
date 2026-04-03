package com.aiplatform.backend.dto;

import java.util.List;

public record ProjectMemberPermissionOverridesResponse(
        Long projectId,
        Long memberId,
        Long userId,
        String role,
        List<ProjectMemberModuleAccessResponse> moduleOverrides,
        List<Long> knowledgeBaseIds,
        List<Long> skillIds,
        List<Long> toolIds,
        List<Long> integrationIds,
        List<Long> atomicCapabilityIds,
        ProjectMemberTokenQuotaSummaryResponse tokenQuotaSummary,
        ProjectMemberAiCapabilitySummaryResponse roleDefaultSummary,
        ProjectMemberAiCapabilitySummaryResponse effectiveSummary
) {
}
