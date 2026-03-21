package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 权限点定义实体，对应数据库 permission_definitions 表。
 *
 * <p>定义平台中所有可分配的权限点，每个权限点归属特定模块，
 * 通过权限编码（permissionKey）唯一标识，如 knowledge.upload、model.call。</p>
 */
@TableName("permission_definitions")
public class PermissionDefinition {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限所属模块，如 knowledge、model、gateway */
    private String module;

    /** 权限点编码，全局唯一，如 knowledge.upload */
    private String permissionKey;

    /** 权限点名称，用于界面展示 */
    private String name;

    /** 权限点描述 */
    private String description;

    /** 适用范围：PLATFORM（平台级）或 PROJECT（项目级） */
    private String permissionScope;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getPermissionKey() { return permissionKey; }
    public void setPermissionKey(String permissionKey) { this.permissionKey = permissionKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPermissionScope() { return permissionScope; }
    public void setPermissionScope(String permissionScope) { this.permissionScope = permissionScope; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
