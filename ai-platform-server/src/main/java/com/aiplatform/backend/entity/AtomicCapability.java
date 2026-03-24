package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 原子能力实体，对应 atomic_capabilities 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("atomic_capabilities")
public class AtomicCapability {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private String category;
    private String docContent;
    private String apiSpecUrl;
    private String gitRepoUrl;
    private String version;
    private String supportedLanguages;
    private Integer subscriptionCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
