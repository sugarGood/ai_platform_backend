package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 模型实体，对应 ai_models 表。
 * <p>记录各供应商下的具体 AI 模型信息，包括定价、上下文窗口等参数。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_models")
public class AiModel {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属供应商ID，关联 ai_providers 表 */
    private Long providerId;

    /** 模型标识编码，如 claude-opus-4-6 */
    private String code;

    /** 模型展示名称 */
    private String name;

    /** 模型系列，如 Claude、GPT */
    private String modelFamily;

    /** 上下文窗口大小（Token 数） */
    private Integer contextWindow;

    /** 输入价格，单位：美元/百万 Token */
    private BigDecimal inputPricePer1m;

    /** 输出价格，单位：美元/百万 Token */
    private BigDecimal outputPricePer1m;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

}
