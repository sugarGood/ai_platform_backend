package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 知识库或关联资源不存在（当前实现中文档缺失也复用此异常）。
 */
public class KnowledgeBaseNotFoundException extends BusinessException {

    public KnowledgeBaseNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.KNOWLEDGE_BASE_NOT_FOUND,
                "知识库不存在: " + id);
    }
}
