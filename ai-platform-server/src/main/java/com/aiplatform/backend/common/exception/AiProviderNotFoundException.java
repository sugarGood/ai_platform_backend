package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * AI 供应商不存在时抛出的异常。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AiProviderNotFoundException extends RuntimeException {

    /**
     * 根据供应商ID构造异常。
     *
     * @param providerId 未找到的供应商ID
     */
    public AiProviderNotFoundException(Long providerId) {
        super("AI provider not found: " + providerId);
    }
}
