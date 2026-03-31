package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 项目订阅原子能力关联，对应 {@code project_atomic_capabilities} 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_atomic_capabilities")
public class ProjectAtomicCapability {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long atomicCapabilityId;

    /** ACTIVE / DISABLED */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
