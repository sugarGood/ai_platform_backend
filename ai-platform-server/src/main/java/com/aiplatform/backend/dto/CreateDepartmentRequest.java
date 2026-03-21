package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建部门的请求参数。
 *
 * @param name        部门名称（必填）
 * @param code        部门编码，用于唯一标识
 * @param description 部门描述
 */
public record CreateDepartmentRequest(
        @NotBlank(message = "Department name must not be blank")
        String name,
        String code,
        String description
) {
}
