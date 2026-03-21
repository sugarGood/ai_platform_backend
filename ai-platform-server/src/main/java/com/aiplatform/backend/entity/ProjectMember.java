package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 项目成员实体，对应数据库 {@code project_members} 表。
 *
 * <p>记录用户与项目的关联关系及其在项目中的角色。</p>
 */
@TableName("project_members")
public class ProjectMember {

    @TableId(type = IdType.AUTO)
    private Long id;              // 成员记录主键 ID（自增）
    private Long projectId;       // 所属项目 ID
    private Long userId;          // 用户 ID
    private String role;          // 角色：ADMIN / MEMBER / VIEWER
    private LocalDateTime joinedAt;   // 加入项目时间
    private LocalDateTime createdAt;  // 记录创建时间
    private LocalDateTime updatedAt;  // 记录更新时间

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
