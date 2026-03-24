package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 工作流定义实体，对应 workflow_definitions 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("workflow_definitions")
public class WorkflowDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private String definitionJson; // 画布JSON
    private String status; // DRAFT / PUBLISHED / ARCHIVED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
