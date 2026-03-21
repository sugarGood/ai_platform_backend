package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 工具定义不存在异常。
 *
 * <p>当根据ID查询工具定义但未找到记录时抛出，HTTP 状态码返回 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ToolDefinitionNotFoundException extends RuntimeException {

    /**
     * 构造工具定义不存在异常。
     *
     * @param id 未找到的工具定义ID
     */
    public ToolDefinitionNotFoundException(Long id) {
        super("Tool definition not found: " + id);
    }
}
