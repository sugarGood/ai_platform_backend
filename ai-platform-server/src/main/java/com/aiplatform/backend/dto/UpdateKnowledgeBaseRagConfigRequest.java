package com.aiplatform.backend.dto;

public record UpdateKnowledgeBaseRagConfigRequest(
        String embeddingModel,
        String injectMode
) {
}
