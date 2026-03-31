package com.aiplatform.backend.dto;

/**
 * 项目工作台卡片分页查询参数。
 *
 * @param page 页码，从 1 开始
 * @param size 每页条数
 * @param includeArchived 未显式指定状态时是否包含归档项目
 * @param keyword 关键字，匹配名称、编码、描述和 ID
 * @param status 状态筛选：ALL / ACTIVE / ARCHIVED
 * @param projectType 项目类型筛选：ALL / PRODUCT / PLATFORM / DATA / OTHER
 */
public record ProjectDashboardQuery(
        int page,
        int size,
        boolean includeArchived,
        String keyword,
        String status,
        String projectType
) {
}
