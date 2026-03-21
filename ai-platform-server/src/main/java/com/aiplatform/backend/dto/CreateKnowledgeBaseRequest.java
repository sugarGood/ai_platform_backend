package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建知识库请求 DTO。
 *
 * @param name           知识库名称（必填）
 * @param description    知识库描述
 * @param scope          作用域：GLOBAL（全局）/ PROJECT（项目级）（必填）
 * @param projectId      所属项目ID，scope 为 PROJECT 时需指定
 * @param category       知识库分类
 * @param embeddingModel 向量化模型名称，默认 bge-m3
 * @param injectMode     注入模式：AUTO_INJECT / ON_DEMAND / DISABLED，默认 ON_DEMAND
 */
public record CreateKnowledgeBaseRequest(
        @NotBlank String name,
        String description,
        @NotBlank String scope,
        Long projectId,
        String category,
        String embeddingModel,
        String injectMode
) {
}
