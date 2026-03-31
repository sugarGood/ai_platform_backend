package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 告警事件实体，对应 alert_events 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alert_events")
public class AlertEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private Long projectId;
    private Long userId;
    private String triggerValue;
    private String message;
    private String notifiedChannels;
    /** 来自关联 {@code alert_rules.severity}，非 alert_events 表字段。 */
    @TableField(exist = false)
    private String severity;
    private String status;  // FIRING / ACKNOWLEDGED / RESOLVED
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

}
