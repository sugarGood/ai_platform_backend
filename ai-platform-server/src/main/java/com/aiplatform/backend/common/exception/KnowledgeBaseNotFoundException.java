package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 知识库不存在异常。
 *
 * <p>当根据ID查询知识库但未找到记录时抛出，HTTP 状态码返回 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class KnowledgeBaseNotFoundException extends RuntimeException {

    /**
     * 构造知识库不存在异常。
     *
     * @param id 未找到的知识库ID
     */
    public KnowledgeBaseNotFoundException(Long id) {
        super("Knowledge base not found: " + id);
    }
}
