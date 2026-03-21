package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 项目成员不存在异常。
 *
 * <p>当根据项目 ID 和成员 ID 查询成员但未找到对应记录时抛出，返回 HTTP 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectMemberNotFoundException extends RuntimeException {

    /**
     * 构造项目成员不存在异常。
     *
     * @param projectId 项目 ID
     * @param memberId  未找到的成员记录 ID
     */
    public ProjectMemberNotFoundException(Long projectId, Long memberId) {
        super("Project member not found: projectId=%d, memberId=%d".formatted(projectId, memberId));
    }
}
