package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.ProviderApiKey;

import java.time.LocalDateTime;

/**
 * 上游 API 密钥响应 DTO。
 * <p>不包含加密后的密钥内容，仅展示密钥前缀，确保安全性。</p>
 *
 * @param id                 密钥ID
 * @param providerId         所属供应商ID
 * @param label              密钥标识名称
 * @param keyPrefix          密钥前缀（用于安全展示）
 * @param modelsAllowed      可用模型列表
 * @param monthlyQuotaTokens 月度 Token 配额
 * @param usedTokensMonth    本月已使用的 Token 数量
 * @param rateLimitRpm       每分钟请求数限制
 * @param rateLimitTpm       每分钟 Token 数限制
 * @param proxyEndpoint      代理端点URL
 * @param status             状态
 * @param createdAt          创建时间
 * @param updatedAt          更新时间
 */
public record ProviderApiKeyResponse(
        Long id,
        Long providerId,
        String label,
        String keyPrefix,
        String modelsAllowed,
        Long monthlyQuotaTokens,
        Long usedTokensMonth,
        Integer rateLimitRpm,
        Integer rateLimitTpm,
        String proxyEndpoint,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将 API 密钥实体转换为响应 DTO。
     *
     * @param key API 密钥实体
     * @return API 密钥响应 DTO
     */
    public static ProviderApiKeyResponse from(ProviderApiKey key) {
        return new ProviderApiKeyResponse(
                key.getId(),
                key.getProviderId(),
                key.getLabel(),
                key.getKeyPrefix(),
                key.getModelsAllowed(),
                key.getMonthlyQuotaTokens(),
                key.getUsedTokensMonth(),
                key.getRateLimitRpm(),
                key.getRateLimitTpm(),
                key.getProxyEndpoint(),
                key.getStatus(),
                key.getCreatedAt(),
                key.getUpdatedAt()
        );
    }
}
