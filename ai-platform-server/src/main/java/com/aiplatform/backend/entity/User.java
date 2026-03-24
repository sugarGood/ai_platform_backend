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
 * 平台用户实体，对应 users 表。
 * <p>记录平台中所有注册用户的基本信息、所属部门及平台角色。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 邮箱地址，用作登录标识 */
    private String email;

    /** 用户名 */
    private String username;

    /** 用户姓名 */
    private String fullName;

    /** 头像URL */
    private String avatarUrl;

    /** 所属部门ID，关联 departments 表 */
    private Long departmentId;

    /** 职位 */
    private String jobTitle;

    /** 手机号 */
    private String phone;

    /**
     * 平台角色 ID，关联 {@code roles.id}（role_scope = PLATFORM）。
     * <p>与 {@code platformRole} 枚举字段并存：
     * {@code platformRole} 用于快速枚举过滤，{@code roleId} 用于精确权限查询。</p>
     */
    private Long roleId;

    /** 平台角色快捷字段：SUPER_ADMIN / PLATFORM_ADMIN / MEMBER（与 roles.code 保持一致） */
    private String platformRole;

    /** BCrypt 哈希后的密码，不返回给前端 */
    private String passwordHash;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

}
