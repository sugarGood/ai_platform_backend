package com.aiplatform.backend.dto;

import java.time.LocalDateTime;

/**
 * 项目调用活动日志查询参数。
 *
 * @param sourceType 用量来源，如 PLATFORM_GATEWAY、MCP_TOOL
 * @param status 调用状态，如 SUCCESS、BLOCKED_BY_QUOTA
 * @param occurredAfter 起始时间，含边界
 * @param occurredBefore 结束时间，含边界
 * @param page 页码，从 1 开始
 * @param size 每页条数
 */
public record ProjectUsageActivityQuery(
        String sourceType,
        String status,
        LocalDateTime occurredAfter,
        LocalDateTime occurredBefore,
        int page,
        int size
) {
}
