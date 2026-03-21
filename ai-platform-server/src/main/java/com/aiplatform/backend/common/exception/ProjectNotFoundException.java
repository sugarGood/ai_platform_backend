package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 项目不存在异常。
 *
 * <p>当根据 ID 查询项目但数据库中不存在对应记录时抛出，返回 HTTP 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundException extends RuntimeException {

    /**
     * 构造项目不存在异常。
     *
     * @param projectId 未找到的项目 ID
     */
    public ProjectNotFoundException(Long projectId) {
        super("Project not found: " + projectId);
    }
}
