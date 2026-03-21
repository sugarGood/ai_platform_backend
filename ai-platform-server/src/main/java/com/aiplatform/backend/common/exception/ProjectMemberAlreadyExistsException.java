package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 项目成员已存在异常。
 *
 * <p>当尝试向项目中添加已存在的用户时抛出，返回 HTTP 409 Conflict。</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ProjectMemberAlreadyExistsException extends RuntimeException {

    /**
     * 构造项目成员已存在异常。
     *
     * @param projectId 项目 ID
     * @param userId    重复添加的用户 ID
     */
    public ProjectMemberAlreadyExistsException(Long projectId, Long userId) {
        super("Member with userId " + userId + " already exists in project " + projectId);
    }
}
