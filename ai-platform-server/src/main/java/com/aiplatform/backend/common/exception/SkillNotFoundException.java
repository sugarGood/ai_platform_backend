package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 技能不存在。 */
public class SkillNotFoundException extends BusinessException {

    public SkillNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.SKILL_NOT_FOUND, "技能不存在: " + id);
    }
}
