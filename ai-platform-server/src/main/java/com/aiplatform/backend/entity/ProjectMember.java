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
 * 项目成员实体，对应数据库 {@code project_members} 表。
 *
 * <p>记录用户与项目的关联关系及其在项目中的角色。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
