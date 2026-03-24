package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 项目不存在。 */
public class ProjectNotFoundException extends BusinessException {

    public ProjectNotFoundException(Long projectId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PROJECT_NOT_FOUND,
                "项目不存在: " + projectId);
    }
}
