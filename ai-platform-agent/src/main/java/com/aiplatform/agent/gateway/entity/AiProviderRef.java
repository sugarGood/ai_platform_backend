package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 供应商引用实体（网关只读视图）。
 *
 * <p>映射 {@code ai_providers} 表，网关模块通过该实体获取上游 AI 供应商的配置信息，
 * 包括供应商编码、API 基础地址等，用于路由请求到正确的供应商端点。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_providers")
public class AiProviderRef {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商编码（如 openai、anthropic），全局唯一标识
     */
    private String code;

    /**
     * 供应商名称
     */
    private String name;

    /**
     * 供应商类型（如 CLOUD、SELF_HOSTED）
     */
    private String providerType;

    /**
     * API 基础 URL，用于拼接上游请求地址
     */
    private String baseUrl;

    /**
     * 供应商状态（如 ACTIVE、DISABLED）
     */
    private String status;

}
