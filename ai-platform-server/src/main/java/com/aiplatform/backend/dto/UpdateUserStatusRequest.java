package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for updating a user's account status.
 */
public record UpdateUserStatusRequest(
        @NotBlank(message = "Status must not be blank")
        @Pattern(regexp = "ACTIVE|DISABLED", message = "Status must be ACTIVE or DISABLED")
        String status
) {
}
