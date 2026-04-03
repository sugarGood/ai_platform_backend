package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.dto.UsageRecordCommand;
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
     */
    public void record(UsageRecordCommand command) {
        AiUsageEventRef event = AiUsageEventRef.builder()
                .credentialId(command.credentialId())
                .userId(command.userId())
                .projectId(command.projectId())
                .providerId(command.providerId())
                .providerApiKeyId(command.providerApiKeyId())
                .modelId(command.modelId())
                .sourceType("PLATFORM_GATEWAY")
                .requestMode(command.requestMode())
                .requestId(command.requestId())
                .inputTokens(command.inputTokens())
                .outputTokens(command.outputTokens())
                .totalTokens(command.totalTokens())
                .costAmount(calculateCost(
                        command.inputTokens(),
                        command.outputTokens(),
                        command.inputPricePer1m(),
                        command.outputPricePer1m()))
                .quotaCheckResult(command.quotaCheckResult())
                .status("SUCCESS")
                .latencyMs((int) command.latencyMs())
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
