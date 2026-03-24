package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 工具定义不存在。 */
public class ToolDefinitionNotFoundException extends BusinessException {

    public ToolDefinitionNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.TOOL_DEFINITION_NOT_FOUND,
                "工具定义不存在: " + id);
    }
}
