package com.aiplatform.backend.dto;

public record ProjectMemberRoleUpdateResponse(
        ProjectMemberResponse member,
        boolean overridesReset,
        ProjectMemberAiCapabilitySummaryResponse roleDefaultSummary,
        ProjectMemberAiCapabilitySummaryResponse effectiveSummary
) {
}
