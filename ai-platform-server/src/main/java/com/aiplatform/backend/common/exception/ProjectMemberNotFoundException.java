package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 项目成员不存在。 */
public class ProjectMemberNotFoundException extends BusinessException {

    public ProjectMemberNotFoundException(Long projectId, Long memberId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PROJECT_MEMBER_NOT_FOUND,
                "项目成员不存在: projectId=%d, memberId=%d".formatted(projectId, memberId));
    }

    public ProjectMemberNotFoundException(Long projectId, Long userId, boolean byUserId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PROJECT_MEMBER_NOT_FOUND,
                "项目成员不存在: projectId=%d, userId=%d".formatted(projectId, userId));
    }
}
