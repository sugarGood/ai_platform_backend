package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建故障转移策略的请求参数。
 *
 * @param name             策略名称（必填）
 * @param primaryKeyId     主用 API 密钥ID（必填）
 * @param fallbackKeyId    备用 API 密钥ID（必填）
 * @param triggerCondition 触发条件（必填），如 ERROR_RATE、LATENCY
 * @param triggerThreshold 触发阈值
 * @param autoRecovery     是否自动恢复到主用密钥
 */
public record CreateProviderFailoverPolicyRequest(
        @NotBlank(message = "Policy name must not be blank")
        String name,
        @NotNull(message = "Primary key ID must not be null")
        Long primaryKeyId,
        @NotNull(message = "Fallback key ID must not be null")
        Long fallbackKeyId,
        @NotBlank(message = "Trigger condition must not be blank")
        String triggerCondition,
        String triggerThreshold,
        Boolean autoRecovery
) {
}
