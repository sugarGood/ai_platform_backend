package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 供应商实体，对应 ai_providers 表。
 * <p>记录平台接入的各 AI 服务供应商信息，如 OpenAI、Anthropic、Google 等。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_providers")
public class AiProvider {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商编码，如 anthropic、openai */
    private String code;

    /** 供应商名称 */
    private String name;

    /** 供应商类型：OPENAI / ANTHROPIC / GOOGLE 等 */
    private String providerType;

    /** API 基础URL */
    private String baseUrl;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

}
