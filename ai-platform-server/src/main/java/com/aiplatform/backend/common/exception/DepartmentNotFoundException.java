package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 部门不存在。 */
public class DepartmentNotFoundException extends BusinessException {

    public DepartmentNotFoundException(Long departmentId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.DEPARTMENT_NOT_FOUND,
                "部门不存在: " + departmentId);
    }
}
