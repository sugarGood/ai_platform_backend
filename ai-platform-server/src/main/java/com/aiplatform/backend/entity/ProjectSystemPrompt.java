package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 项目 System Prompt 实体，对应 project_system_prompts 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_system_prompts")
public class ProjectSystemPrompt {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String promptName;
    private String promptType;   // GLOBAL_INJECT / PROJECT_CONTEXT / CODING_STANDARD / SECURITY_RULES / CUSTOM
    private String content;
    private String injectStrategy; // ALWAYS / ON_DEMAND / DISABLED
    private Integer maxTokens;
    private Integer priority;
    private String status;       // ACTIVE / DISABLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
