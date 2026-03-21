package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 项目工具启用实体，对应 project_tools 表。
 *
 * <p>记录项目与工具定义的关联关系，表示项目启用了哪些工具。</p>
 */
@TableName("project_tools")
public class ProjectTool {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目ID */
    private Long projectId;

    /** 工具定义ID */
    private Long toolId;

    /** 状态：ACTIVE / INACTIVE */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
