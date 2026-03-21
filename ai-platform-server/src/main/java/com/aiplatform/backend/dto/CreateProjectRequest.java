package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 创建项目的请求参数。
 *
 * @param name        项目名称（必填）
 * @param code        项目编码，唯一标识（必填）
 * @param description 项目描述
 * @param icon        项目图标（emoji 或 icon name）
 * @param projectType 项目类型：PRODUCT / PLATFORM / DATA / OTHER（必填）
 * @param ownerUserId 项目负责人用户 ID
 */
public record CreateProjectRequest(
        @NotBlank(message = "Project name must not be blank")
        String name,
        @NotBlank(message = "Project code must not be blank")
        String code,
        String description,
        String icon,
        @NotBlank(message = "Project type must not be blank")
        @Pattern(regexp = "PRODUCT|PLATFORM|DATA|OTHER", message = "Invalid project type")
        String projectType,
        Long ownerUserId
) {
}
