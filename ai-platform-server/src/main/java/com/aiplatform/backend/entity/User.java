package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 平台用户实体，对应 users 表。
 * <p>记录平台中所有注册用户的基本信息、所属部门及平台角色。</p>
 */
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

    /** 平台角色：SUPER_ADMIN（超级管理员）/ PLATFORM_ADMIN（平台管理员）/ MEMBER（普通成员） */
    private String platformRole;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPlatformRole() { return platformRole; }
    public void setPlatformRole(String platformRole) { this.platformRole = platformRole; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
