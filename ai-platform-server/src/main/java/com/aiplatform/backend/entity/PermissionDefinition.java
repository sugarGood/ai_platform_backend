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
 * 权限点定义实体，对应 {@code permission_definitions} 表。
 *
 * <p>每条记录代表系统中一个最细粒度的操作权限，
 * 如 {@code knowledge.upload}、{@code skill.publish}、{@code quota.manage} 等。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("permission_definitions")
public class PermissionDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限模块，如 knowledge / skill / tool / quota / credential */
    private String module;

    /**
     * 权限点编码，格式为 {@code 模块.操作}，全局唯一。
     * 示例：{@code knowledge.upload}、{@code skill.publish}
     */
    private String permissionKey;

    /** 权限显示名称 */
    private String name;

    /** 权限描述 */
    private String description;

    /**
     * 权限适用范围：PLATFORM（平台级）/ PROJECT（项目级）/ BOTH（均适用）。
     *
     * <p>同时也作为 {@code rbac_role_permissions} 中角色-权限关联的查询键。
     * 在 RBAC 切面中，{@code permissionKey} 与 {@code permission_code} 均可用，
     * 新权限点建议同时在本表和 {@code patch_02_rbac_init.sql} 的
     * {@code permission_definitions} 中保持一致。</p>
     */
    private String permissionScope;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 获取 RBAC 切面使用的权限编码。
     *
     * <p>兼容方法：{@code RbacService} 使用 {@code permissionKey} 作为权限编码，
     * 与 {@code rbac_role_permissions.permission_code} 保持一致即可。</p>
     *
     * @return 权限编码（同 permissionKey）
     */
    public String getPermissionCode() { return permissionKey; }
}
