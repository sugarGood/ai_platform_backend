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
 * 项目角色模板实体，对应 {@code project_role_templates} 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_role_templates")
public class ProjectRoleTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String roleCode;

    private String templateName;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
