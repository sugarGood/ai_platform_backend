package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.ProviderFailoverPolicy;

import java.time.LocalDateTime;

/**
 * 故障转移策略响应 DTO。
 * <p>封装故障转移策略实体的全部字段，用于 REST 接口返回。</p>
 *
 * @param id               策略ID
 * @param name             策略名称
 * @param primaryKeyId     主用 API 密钥ID
 * @param fallbackKeyId    备用 API 密钥ID
 * @param triggerCondition 触发条件
 * @param triggerThreshold 触发阈值
 * @param autoRecovery     是否自动恢复
 * @param status           状态
 * @param lastTriggeredAt  上次触发时间
 * @param createdAt        创建时间
 * @param updatedAt        更新时间
 */
public record ProviderFailoverPolicyResponse(
        Long id,
        String name,
        Long primaryKeyId,
        Long fallbackKeyId,
        String triggerCondition,
        String triggerThreshold,
        Boolean autoRecovery,
        String status,
        LocalDateTime lastTriggeredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将故障转移策略实体转换为响应 DTO。
     *
     * @param policy 策略实体
     * @return 策略响应 DTO
     */
    public static ProviderFailoverPolicyResponse from(ProviderFailoverPolicy policy) {
        return new ProviderFailoverPolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getPrimaryKeyId(),
                policy.getFallbackKeyId(),
                policy.getTriggerCondition(),
                policy.getTriggerThreshold(),
                policy.getAutoRecovery(),
                policy.getStatus(),
                policy.getLastTriggeredAt(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }
}
