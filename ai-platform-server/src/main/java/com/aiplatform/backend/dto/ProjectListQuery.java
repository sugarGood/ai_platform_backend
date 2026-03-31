package com.aiplatform.backend.dto;

/**
 * 项目分页列表查询参数。
 *
 * @param page 页码，从 1 开始
 * @param size 每页条数
 * @param keyword 关键字，匹配名称、编码、描述和 ID
 * @param status 状态筛选：ALL / ACTIVE / ARCHIVED
 * @param projectType 项目类型筛选：ALL / PRODUCT / PLATFORM / DATA / OTHER
 */
public record ProjectListQuery(
        int page,
        int size,
        String keyword,
        String status,
        String projectType
) {
}
