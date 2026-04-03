package com.aiplatform.backend.dto;

public record ProjectMemberModuleAccessResponse(
        String moduleKey,
        String accessLevel,
        boolean overridden
) {
}
