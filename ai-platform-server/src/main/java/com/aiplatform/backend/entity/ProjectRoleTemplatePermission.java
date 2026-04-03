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
 * 项目角色模板权限矩阵实体，对应 {@code project_role_template_permissions} 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_role_template_permissions")
public class ProjectRoleTemplatePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectRoleTemplateId;

    private String moduleKey;

    private String accessLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
