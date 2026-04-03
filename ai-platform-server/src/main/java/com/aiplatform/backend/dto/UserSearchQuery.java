package com.aiplatform.backend.dto;

/**
 * Query object for filtering users in the admin list view.
 */
public record UserSearchQuery(
        String keyword,
        Long departmentId,
        String platformRole,
        String status
) {
}
