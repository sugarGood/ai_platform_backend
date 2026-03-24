package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 通知渠道实体，对应 notification_channels 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_channels")
public class NotificationChannel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String channelType; // EMAIL / WECHAT_WORK / DINGTALK / SLACK / WEBHOOK / SMS
    private String config;      // JSON
    private Boolean isDefault;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
