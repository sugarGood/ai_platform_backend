package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** AI 模型不存在。 */
public class AiModelNotFoundException extends BusinessException {

    public AiModelNotFoundException(Long modelId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.AI_MODEL_NOT_FOUND,
                "AI 模型不存在: " + modelId);
    }
}
