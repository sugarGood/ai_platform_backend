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
 * 角色-权限关联实体，对应数据库 role_permissions 表。
 *
 * <p>定义角色与权限点之间的多对多关联关系，并指定具体的访问级别，
 * 实现细粒度的权限控制。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("role_permissions")
public class RolePermission {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的角色ID */
    private Long roleId;

    /** 关联的权限点ID */
    private Long permissionId;

    /** 访问级别：NONE / VIEW / CALL / CREATE / FULL_CONTROL */
    private String accessLevel;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

}
