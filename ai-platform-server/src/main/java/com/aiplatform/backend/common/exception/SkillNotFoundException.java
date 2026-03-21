package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 技能不存在异常。
 *
 * <p>当根据ID查询技能但未找到记录时抛出，HTTP 状态码返回 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SkillNotFoundException extends RuntimeException {

    /**
     * 构造技能不存在异常。
     *
     * @param id 未找到的技能ID
     */
    public SkillNotFoundException(Long id) {
        super("Skill not found: " + id);
    }
}
