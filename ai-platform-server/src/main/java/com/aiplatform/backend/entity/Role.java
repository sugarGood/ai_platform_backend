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
 * 角色定义实体，对应数据库 roles 表。
 *
 * <p>定义平台和项目级别的角色信息，包括角色编码、适用范围、
 * 默认Token配额等属性，支持系统内置角色和自定义角色。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
