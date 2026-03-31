package com.aiplatform.backend.dto;

import java.util.List;

/**
 * 项目下知识库来源列表（统一元素类型）：专属与继承的全局库合并为 {@code items}。
 */
public record ProjectKnowledgeSourcesResponse(List<ProjectKnowledgeSourceItem> items) {
}
