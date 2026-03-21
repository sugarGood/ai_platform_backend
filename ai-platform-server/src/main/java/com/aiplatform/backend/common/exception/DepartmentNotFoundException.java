package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 部门不存在时抛出的异常。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DepartmentNotFoundException extends RuntimeException {

    /**
     * 根据部门ID构造异常。
     *
     * @param departmentId 未找到的部门ID
     */
    public DepartmentNotFoundException(Long departmentId) {
        super("Department not found: " + departmentId);
    }
}
