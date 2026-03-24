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
 * 平台角色实体，对应 {@code platform_roles} 表。
 *
 * <p>内置三个系统角色（{@code is_system=1}）不可删除：
 * SUPER_ADMIN、PLATFORM_ADMIN、MEMBER。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("platform_roles")
public class PlatformRole {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，全局唯一，如 SUPER_ADMIN */
    private String roleCode;

    /** 角色显示名称 */
    private String roleName;

    /** 角色描述 */
    private String description;

    /** 是否系统内置（1=是，不可删除） */
    private Integer isSystem;

    /** 状态：ACTIVE / DISABLED */
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
