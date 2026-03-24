package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 告警规则实体，对应 alert_rules 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alert_rules")
public class AlertRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String triggerCondition;  // TOKEN_USAGE / ERROR_RATE / LATENCY / CONSECUTIVE_FAILURE
    private String triggerExpression; // JSON
    private String severity;          // CRITICAL / HIGH / MEDIUM / LOW
    private String notificationChannelIds; // JSON array
    private Integer cooldownMinutes;
    private String scope;             // PLATFORM / PROJECT
    private Long projectId;
    private String status;            // ACTIVE / DISABLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
