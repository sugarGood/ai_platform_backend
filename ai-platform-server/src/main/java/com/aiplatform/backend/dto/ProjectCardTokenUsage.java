package com.aiplatform.backend.dto;

/**
 * 项目卡片「本月 Token」：已用量与上限；{@code limit} 为 {@code null} 表示不限制。
 */
public record ProjectCardTokenUsage(
        long used,
        Long limit
) {
}
