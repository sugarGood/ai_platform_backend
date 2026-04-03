package com.aiplatform.backend.dto.me;

/**
 * 切换当前工作项目请求。
 *
 * @param projectId 项目 ID，传 null 表示清空当前工作项目
 */
public record MeCurrentProjectRequest(
        Long projectId
) {
}
