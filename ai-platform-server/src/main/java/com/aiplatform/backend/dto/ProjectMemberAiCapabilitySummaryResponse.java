package com.aiplatform.backend.dto;

import java.util.List;

public record ProjectMemberAiCapabilitySummaryResponse(
        Long projectId,
        Long memberId,
        Long userId,
        String role,
        boolean overrideConfigured,
        List<ProjectMemberModuleAccessResponse> moduleAccess,
        ProjectMemberScopedResourceSummaryResponse knowledgeBaseAccessSummary,
        ProjectMemberScopedResourceSummaryResponse skillAccessSummary,
        ProjectMemberScopedResourceSummaryResponse toolAccessSummary,
        ProjectMemberScopedResourceSummaryResponse integrationAccessSummary,
        ProjectMemberScopedResourceSummaryResponse atomicCapabilityAccessSummary,
        ProjectMemberTokenQuotaSummaryResponse tokenQuotaSummary
) {
}
