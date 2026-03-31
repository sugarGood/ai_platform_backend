package com.aiplatform.backend.dto;

import java.math.BigDecimal;

/**
 * 项目下一条知识库来源（统一结构）：项目专属或继承的全局库。
 *
 * @param kind                     {@code DEDICATED} 项目专属；{@code GLOBAL_INHERITED} 继承的全局库
 * @param knowledgeBase            知识库详情（两种来源均有）
 * @param projectKnowledgeConfigId 仅 {@code GLOBAL_INHERITED} 时有值：{@code project_knowledge_configs.id}
 * @param searchWeight             仅继承时有值：检索权重
 * @param configStatus             仅继承时有值：配置状态 ACTIVE / INACTIVE
 * @param injectMode               仅继承时有值：AUTO_INJECT / ON_DEMAND / DISABLED
 */
public record ProjectKnowledgeSourceItem(
        String kind,
        KnowledgeBaseResponse knowledgeBase,
        Long projectKnowledgeConfigId,
        BigDecimal searchWeight,
        String configStatus,
        String injectMode
) {
    public static final String KIND_DEDICATED = "DEDICATED";
    public static final String KIND_GLOBAL_INHERITED = "GLOBAL_INHERITED";

    public static ProjectKnowledgeSourceItem dedicated(KnowledgeBaseResponse kb) {
        return new ProjectKnowledgeSourceItem(KIND_DEDICATED, kb, null, null, null, null);
    }

    public static ProjectKnowledgeSourceItem globalInherited(
            Long configId,
            BigDecimal searchWeight,
            String configStatus,
            String injectMode,
            KnowledgeBaseResponse kb) {
        return new ProjectKnowledgeSourceItem(KIND_GLOBAL_INHERITED, kb, configId, searchWeight, configStatus, injectMode);
    }
}
