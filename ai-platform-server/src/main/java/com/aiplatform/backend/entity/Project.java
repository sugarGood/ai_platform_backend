package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 项目实体，对应数据库 {@code projects} 表。
 *
 * <p>记录平台中各项目的基本信息，包括名称、编码、类型和负责人等。</p>
 */
@TableName("projects")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;              // 项目主键 ID（自增）
    private String name;          // 项目名称
    private String code;          // 项目编码，唯一标识
    private String description;   // 项目描述
    private String icon;          // 项目图标（emoji 或 icon name）
    private String projectType;   // 项目类型：PRODUCT / PLATFORM / DATA / OTHER
    private Long createdBy;       // 创建人用户 ID
    private Long ownerUserId;     // 项目负责人用户 ID
    private String status;        // 项目状态：ACTIVE / ARCHIVED
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 最后更新时间

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
