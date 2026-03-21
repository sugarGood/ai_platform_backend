package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * AI 模型不存在时抛出的异常。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AiModelNotFoundException extends RuntimeException {

    /**
     * 根据模型ID构造异常。
     *
     * @param modelId 未找到的模型ID
     */
    public AiModelNotFoundException(Long modelId) {
        super("AI model not found: " + modelId);
    }
}
