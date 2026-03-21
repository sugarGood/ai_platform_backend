package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.AiUsageEventRef;
import com.aiplatform.agent.gateway.mapper.AiUsageEventRefMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用量记录服务。
 *
 * <p>在网关调用上游供应商成功后，将本次请求的用量信息写入 {@code ai_usage_events} 表，
 * 用于后续的用量统计、配额管控和费用核算。</p>
 */
@Service
public class UsageRecordingService {

    /** 用量事件数据访问 Mapper */
    private final AiUsageEventRefMapper usageEventMapper;

    /**
     * 构造用量记录服务。
     *
     * @param usageEventMapper 用量事件数据访问 Mapper
     */
    public UsageRecordingService(AiUsageEventRefMapper usageEventMapper) {
        this.usageEventMapper = usageEventMapper;
    }

    /**
     * 记录一次 AI 调用的用量事件。
     *
     * <p>当前版本 Token 用量和费用字段暂置为零值，后续可通过解析上游响应获取实际数据。</p>
     *
     * @param credentialId     调用方凭证 ID
     * @param userId           调用方用户 ID
     * @param providerId       上游供应商 ID
     * @param providerApiKeyId 使用的上游 API 密钥 ID
     * @param modelId          调用的模型 ID
     * @param modelCode        模型标识编码
     * @param latencyMs        请求延迟（毫秒）
     */
    public void record(Long credentialId, Long userId, Long providerId,
                       Long providerApiKeyId, Long modelId, String modelCode,
                       long latencyMs) {
        AiUsageEventRef event = new AiUsageEventRef();
        event.setCredentialId(credentialId);
        event.setUserId(userId);
        event.setProviderId(providerId);
        event.setProviderApiKeyId(providerApiKeyId);
        event.setModelId(modelId);
        event.setSourceType("PLATFORM_GATEWAY");
        event.setRequestMode("CHAT");
        event.setInputTokens(0L);
        event.setOutputTokens(0L);
        event.setTotalTokens(0L);
        event.setCostAmount(BigDecimal.ZERO);
        event.setStatus("SUCCESS");
        event.setLatencyMs((int) latencyMs);
        event.setOccurredAt(LocalDateTime.now());
        usageEventMapper.insert(event);
    }
}
