package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 活动日志实体，对应 activity_logs 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_logs")
public class ActivityLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long userId;
    private String actorName;
    private String actionType;
    private String summary;
    private String targetType;
    private Long targetId;
    private String targetName;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;

}
