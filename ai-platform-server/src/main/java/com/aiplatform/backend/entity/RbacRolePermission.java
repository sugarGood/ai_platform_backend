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
 * 角色-权限关联实体，对应 {@code rbac_role_permissions} 表。
 *
 * <p>记录某角色（{@code platform_roles}）拥有哪些权限点（{@code permission_definitions}）。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("rbac_role_permissions")
public class RbacRolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，关联 platform_roles.role_code */
    private String roleCode;

    /** 权限编码，关联 permission_definitions.permission_code */
    private String permissionCode;

    private LocalDateTime createdAt;

}
