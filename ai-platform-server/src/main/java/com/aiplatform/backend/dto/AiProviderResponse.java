package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.AiProvider;

import java.time.LocalDateTime;

/**
 * AI 供应商响应 DTO。
 * <p>封装供应商实体的全部字段，用于 REST 接口返回。</p>
 *
 * @param id           供应商ID
 * @param code         供应商编码
 * @param name         供应商名称
 * @param providerType 供应商类型
 * @param baseUrl      API 基础URL
 * @param status       状态
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 */
public record AiProviderResponse(
        Long id,
        String code,
        String name,
        String providerType,
        String baseUrl,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将供应商实体转换为响应 DTO。
     *
     * @param provider 供应商实体
     * @return 供应商响应 DTO
     */
    public static AiProviderResponse from(AiProvider provider) {
        return new AiProviderResponse(
                provider.getId(),
                provider.getCode(),
                provider.getName(),
                provider.getProviderType(),
                provider.getBaseUrl(),
                provider.getStatus(),
                provider.getCreatedAt(),
                provider.getUpdatedAt()
        );
    }
}
