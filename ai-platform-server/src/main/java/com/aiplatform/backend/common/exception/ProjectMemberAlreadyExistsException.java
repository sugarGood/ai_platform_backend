package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 项目成员已存在。 */
public class ProjectMemberAlreadyExistsException extends BusinessException {

    public ProjectMemberAlreadyExistsException(Long projectId, Long userId) {
        super(HttpStatus.CONFLICT.value(), BizErrorCode.PROJECT_MEMBER_ALREADY_EXISTS,
                "用户已是该项目成员: projectId=%d, userId=%d".formatted(projectId, userId));
    }
}
