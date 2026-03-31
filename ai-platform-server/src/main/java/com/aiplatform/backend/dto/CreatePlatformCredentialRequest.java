package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建平台凭证的请求参数。
 *
 * <p>一人一证、跨项目共用；当前工作项目不在创建请求中指定，由凭证绑定接口单独更新。
 * 平台邀请用户时自动创建 PERSONAL 凭证；SERVICE_ACCOUNT 凭证由管理员手动创建。</p>
 *
 * @param userId              所属用户 ID（必填）
 * @param credentialType      凭证类型：PERSONAL / SERVICE_ACCOUNT / TEMP，默认 PERSONAL
 * @param name                凭证名称（服务账号场景使用，个人凭证可为空）
 * @param monthlyTokenQuota   个人月度 Token 上限，0=不限制，null=使用 job_type 默认策略
 * @param alertThresholdPct   告警阈值百分比（0-100），null=使用默认值 80
 * @param overQuotaStrategy   超配额策略，null=使用默认值 BLOCK
 */
public record CreatePlatformCredentialRequest(
        @NotNull(message = "User ID must not be null")
        Long userId,
        String credentialType,
        String name,
        Long monthlyTokenQuota,
        Integer alertThresholdPct,
        String overQuotaStrategy
) {
}
