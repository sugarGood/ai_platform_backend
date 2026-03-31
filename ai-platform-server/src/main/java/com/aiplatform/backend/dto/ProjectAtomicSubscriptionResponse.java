package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.AtomicCapability;
import com.aiplatform.backend.entity.ProjectAtomicCapability;

/**
 * 项目内原子能力订阅项（关联行 + 能力摘要）。
 *
 * @param id                  关联表主键 {@code project_atomic_capabilities.id}
 * @param projectId           项目 ID
 * @param atomicCapabilityId  原子能力 ID
 * @param status              ACTIVE / DISABLED
 * @param name                能力名称
 * @param code                能力编码
 * @param category            分类
 * @param icon                图标
 */
public record ProjectAtomicSubscriptionResponse(
        Long id,
        Long projectId,
        Long atomicCapabilityId,
        String status,
        String name,
        String code,
        String category,
        String icon
) {
    public static ProjectAtomicSubscriptionResponse from(ProjectAtomicCapability row, AtomicCapability cap) {
        return new ProjectAtomicSubscriptionResponse(
                row.getId(),
                row.getProjectId(),
                row.getAtomicCapabilityId(),
                row.getStatus(),
                cap != null ? cap.getName() : null,
                cap != null ? cap.getCode() : null,
                cap != null ? cap.getCategory() : null,
                cap != null ? cap.getIcon() : null
        );
    }
}
