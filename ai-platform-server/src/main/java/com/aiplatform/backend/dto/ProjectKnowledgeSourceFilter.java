package com.aiplatform.backend.dto;

/**
 * 项目知识库来源筛选：独有（项目专属）与全局（继承的全局库）。
 */
public enum ProjectKnowledgeSourceFilter {

    /** 两者都返回（默认） */
    ALL,

    /** 仅项目专属：{@code scope=PROJECT} 且 {@code project_id} 匹配 */
    DEDICATED,

    /** 仅继承的全局库：{@code project_knowledge_configs} → {@code scope=GLOBAL} */
    GLOBAL_INHERITED
}
