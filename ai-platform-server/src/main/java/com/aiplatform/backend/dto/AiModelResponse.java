package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.AiModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 模型响应 DTO。
 * <p>封装模型实体的全部字段，用于 REST 接口返回。</p>
 *
 * @param id               模型ID
 * @param providerId       所属供应商ID
 * @param code             模型标识编码
 * @param name             模型展示名称
 * @param modelFamily      模型系列
 * @param contextWindow    上下文窗口大小
 * @param inputPricePer1m  输入价格（美元/百万 Token）
 * @param outputPricePer1m 输出价格（美元/百万 Token）
 * @param status           状态
 * @param createdAt        创建时间
 * @param updatedAt        更新时间
 */
public record AiModelResponse(
        Long id,
        Long providerId,
        String code,
        String name,
        String modelFamily,
        Integer contextWindow,
        BigDecimal inputPricePer1m,
        BigDecimal outputPricePer1m,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将模型实体转换为响应 DTO。
     *
     * @param model 模型实体
     * @return 模型响应 DTO
     */
    public static AiModelResponse from(AiModel model) {
        return new AiModelResponse(
                model.getId(),
                model.getProviderId(),
                model.getCode(),
                model.getName(),
                model.getModelFamily(),
                model.getContextWindow(),
                model.getInputPricePer1m(),
                model.getOutputPricePer1m(),
                model.getStatus(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}
