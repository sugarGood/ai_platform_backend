package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建平台凭证的请求参数。
 *
 * @param userId         所属用户 ID（必填）
 * @param credentialType 凭证类型：PERSONAL / SERVICE_ACCOUNT / TEMPORARY，默认 PERSONAL
 * @param name           凭证名称（服务账号场景使用）
 * @param boundProjectId 绑定项目 ID（服务账号可绑定单项目）
 */
public record CreatePlatformCredentialRequest(
        @NotNull(message = "User ID must not be null")
        Long userId,
        String credentialType,
        String name,
        Long boundProjectId
) {
}
