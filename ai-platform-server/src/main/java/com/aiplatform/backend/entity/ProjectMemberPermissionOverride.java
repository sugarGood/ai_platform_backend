package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_member_permission_overrides")
public class ProjectMemberPermissionOverride {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectMemberId;

    private String moduleKey;

    private String accessLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
