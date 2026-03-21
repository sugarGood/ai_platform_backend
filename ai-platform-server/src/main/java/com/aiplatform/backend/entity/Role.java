package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 角色定义实体，对应数据库 roles 表。
 *
 * <p>定义平台和项目级别的角色信息，包括角色编码、适用范围、
 * 默认Token配额等属性，支持系统内置角色和自定义角色。</p>
 */
@TableName("roles")
public class Role {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称，如"超级管理员"、"项目成员" */
    private String name;

    /** 角色编码，唯一标识，如 SUPER_ADMIN、PROJECT_MEMBER */
    private String code;

    /** 适用范围：PLATFORM（平台级）或 PROJECT（项目级） */
    private String roleScope;

    /** 角色描述 */
    private String description;

    /** 是否为系统内置角色（内置角色不可删除） */
    private Boolean isSystem;

    /** 该角色的默认月Token配额（单位：Token数） */
    private Long defaultQuotaTokens;

    /** 角色状态，如 ACTIVE、DISABLED */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getRoleScope() { return roleScope; }
    public void setRoleScope(String roleScope) { this.roleScope = roleScope; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }
    public Long getDefaultQuotaTokens() { return defaultQuotaTokens; }
    public void setDefaultQuotaTokens(Long defaultQuotaTokens) { this.defaultQuotaTokens = defaultQuotaTokens; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
