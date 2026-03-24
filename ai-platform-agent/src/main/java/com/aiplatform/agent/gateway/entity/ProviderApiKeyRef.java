package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上游 API 密钥引用实体（网关只读视图）。
 *
 * <p>映射 {@code provider_api_keys} 表，存储平台向上游 AI 供应商发起请求时
 * 所使用的 API 密钥。密钥以加密形式存储，网关在调用时解密使用。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("provider_api_keys")
public class ProviderApiKeyRef {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属供应商 ID
     */
    private Long providerId;

    /**
     * 密钥标识名称，便于管理员区分多个密钥
     */
    private String label;

    /**
     * 加密存储的 API Key，调用上游时解密使用
     */
    private String apiKeyEncrypted;

    /**
     * 密钥状态（如 ACTIVE、DISABLED）
     */
    private String status;

}
