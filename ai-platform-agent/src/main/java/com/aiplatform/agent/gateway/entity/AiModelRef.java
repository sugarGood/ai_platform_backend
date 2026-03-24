package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 模型引用实体（网关只读视图）。
 *
 * <p>映射 {@code ai_models} 表，网关模块通过该实体查找请求中指定的模型，
 * 并据此确定对应的上游供应商，完成路由决策。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_models")
public class AiModelRef {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属供应商 ID */
    private Long providerId;

    /** 模型标识编码（如 gpt-4o、claude-3），请求时使用 */
    private String code;

    /** 模型名称 */
    private String name;

    /** 每百万输入 Token 单价 */
    private BigDecimal inputPricePer1m;

    /** 每百万输出 Token 单价 */
    private BigDecimal outputPricePer1m;

    /** 模型状态（如 ACTIVE、DEPRECATED） */
    private String status;

}
