package com.aiplatform.backend.dto;

/**
 * 项目卡片页「AI 能力」区块：技能 / 工具 / 知识库数量。
 */
public record ProjectCardAiMetrics(
        int skills,
        int tools,
        int knowledgeBase
) {
}
