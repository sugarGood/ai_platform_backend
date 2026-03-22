package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.AiUsageEventRef;
import com.aiplatform.agent.gateway.mapper.AiUsageEventRefMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 用量记录服务。
 *
 * <p>在网关调用上游供应商成功后，将本次请求的用量信息写入 {@code ai_usage_events} 表，
 * 用于后续的用量统计、配额管控和费用核算。</p>
 */
@Service
public class UsageRecordingService {

    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");

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
     * @param credentialId     调用方凭证 ID
     * @param userId           调用方用户 ID
     * @param projectId        关联项目 ID，可为 null
     * @param providerId       上游供应商 ID
     * @param providerApiKeyId 使用的上游 API 密钥 ID
     * @param modelId          调用的模型 ID
     * @param modelCode        模型标识编码
     * @param requestMode      请求模式（CHAT=同步 / STREAM=流式）
     * @param latencyMs        请求延迟（毫秒）
     * @param inputTokens      输入 Token 数
     * @param outputTokens     输出 Token 数
     * @param totalTokens      总 Token 数
     * @param inputPricePer1m  每百万输入 Token 单价，可为 null
     * @param outputPricePer1m 每百万输出 Token 单价，可为 null
     */
    public void record(Long credentialId, Long userId, Long projectId, Long providerId,
                       Long providerApiKeyId, Long modelId, String modelCode,
                       String requestMode,
                       long latencyMs, long inputTokens, long outputTokens,
                       long totalTokens, BigDecimal inputPricePer1m, BigDecimal outputPricePer1m) {
        AiUsageEventRef event = AiUsageEventRef.builder()
                .credentialId(credentialId)
                .userId(userId)
                .projectId(projectId)
                .providerId(providerId)
                .providerApiKeyId(providerApiKeyId)
                .modelId(modelId)
                .sourceType("PLATFORM_GATEWAY")
                .requestMode(requestMode)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(totalTokens)
                .costAmount(calculateCost(inputTokens, outputTokens, inputPricePer1m, outputPricePer1m))
                .status("SUCCESS")
                .latencyMs((int) latencyMs)
                .occurredAt(LocalDateTime.now())
                .build();
        usageEventMapper.insert(event);
    }

    /**
     * 计算本次调用费用。
     *
     * <p>公式：cost = inputTokens * inputPrice / 1M + outputTokens * outputPrice / 1M</p>
     */
    private BigDecimal calculateCost(long inputTokens, long outputTokens,
                                      BigDecimal inputPricePer1m, BigDecimal outputPricePer1m) {
        BigDecimal cost = BigDecimal.ZERO;
        if (inputPricePer1m != null && inputTokens > 0) {
            cost = cost.add(BigDecimal.valueOf(inputTokens)
                    .multiply(inputPricePer1m)
                    .divide(ONE_MILLION, 6, RoundingMode.HALF_UP));
        }
        if (outputPricePer1m != null && outputTokens > 0) {
            cost = cost.add(BigDecimal.valueOf(outputTokens)
                    .multiply(outputPricePer1m)
                    .divide(ONE_MILLION, 6, RoundingMode.HALF_UP));
        }
        return cost;
    }
}
