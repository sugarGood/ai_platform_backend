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
 * 供应商故障转移策略实体，对应 provider_failover_policies 表。
 * <p>定义当主用 API 密钥出现故障时，自动切换到备用密钥的策略规则。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("provider_failover_policies")
public class ProviderFailoverPolicy {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 策略名称 */
    private String name;

    /** 主用 API 密钥ID，关联 provider_api_keys 表 */
    private Long primaryKeyId;

    /** 备用 API 密钥ID，关联 provider_api_keys 表 */
    private Long fallbackKeyId;

    /** 触发条件，如 ERROR_RATE（错误率）、LATENCY（延迟） */
    private String triggerCondition;

    /** 触发阈值 */
    private String triggerThreshold;

    /** 是否自动恢复到主用密钥 */
    private Boolean autoRecovery;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 上次触发时间 */
    private LocalDateTime lastTriggeredAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

}
