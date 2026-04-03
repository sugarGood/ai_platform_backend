package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProjectMemberPermissionOverrideItemRequest(
        @NotBlank
        @Pattern(regexp = "KNOWLEDGE_BASE|SKILL|TOOL|INTEGRATION|ATOMIC_CAPABILITY|QUOTA",
                message = "Invalid module key")
        String moduleKey,
        @NotBlank
        @Pattern(regexp = "NONE|VIEW|CALL|FULL_CONTROL", message = "Invalid access level")
        String accessLevel
) {
}
