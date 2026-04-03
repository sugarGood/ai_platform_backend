package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PlatformSettingValueRequest(
        @NotBlank(message = "value must not be blank")
        String value
) {
}
