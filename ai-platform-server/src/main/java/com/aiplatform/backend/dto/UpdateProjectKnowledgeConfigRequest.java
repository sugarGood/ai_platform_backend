package com.aiplatform.backend.dto;

import java.math.BigDecimal;

/**
 * 更新项目知识库绑定配置（字段均可选，仅非 null 字段参与更新）。
 *
 * @param searchWeight 检索权重 0~1
 * @param injectMode   AUTO_INJECT / ON_DEMAND / DISABLED
 * @param status       ACTIVE / DISABLED（与表 {@code project_knowledge_configs.status} 枚举一致）
 */
public record UpdateProjectKnowledgeConfigRequest(
        BigDecimal searchWeight,
        String injectMode,
        String status
) {
}
