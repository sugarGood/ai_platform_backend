package com.aiplatform.backend.dto.me;

/**
 * 当前用户可访问项目选项。
 *
 * @param id   项目 ID
 * @param name 项目名称
 * @param code 项目编码
 */
public record MeProjectOptionResponse(
        Long id,
        String name,
        String code
) {
}
