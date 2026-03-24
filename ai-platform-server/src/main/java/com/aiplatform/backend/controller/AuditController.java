package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.entity.ActivityLog;
import com.aiplatform.backend.mapper.ActivityLogMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计与安全控制器（模块15）。
 *
 * <p>提供项目活动日志的查询和写入接口。</p>
 */
@RestController
public class AuditController {

    private final ActivityLogMapper activityLogMapper;

    public AuditController(ActivityLogMapper activityLogMapper) {
        this.activityLogMapper = activityLogMapper;
    }

    /** 查询项目活动日志（分页）。 */
    @GetMapping("/api/projects/{projectId}/activity-logs")
    public PageResponse<ActivityLog> projectActivityLogs(
            @PathVariable Long projectId,
            @RequestParam(required = false) String actionType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var q = Wrappers.<ActivityLog>lambdaQuery()
                .eq(ActivityLog::getProjectId, projectId);
        if (actionType != null && !actionType.isBlank())
            q.eq(ActivityLog::getActionType, actionType);
        q.orderByDesc(ActivityLog::getOccurredAt);
        var result = activityLogMapper.selectPage(new Page<>(page, size), q);
        return PageResponse.from(result, a -> a);
    }

    /** 查询平台审计日志（管理员，分页）。 */
    @GetMapping("/api/admin/audit-logs")
    public PageResponse<ActivityLog> adminAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String actionType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var q = Wrappers.<ActivityLog>lambdaQuery();
        if (userId != null) q.eq(ActivityLog::getUserId, userId);
        if (actionType != null && !actionType.isBlank()) q.eq(ActivityLog::getActionType, actionType);
        q.orderByDesc(ActivityLog::getOccurredAt);
        var result = activityLogMapper.selectPage(new Page<>(page, size), q);
        return PageResponse.from(result, a -> a);
    }

    /** 查询单条审计日志详情。 */
    @GetMapping("/api/admin/audit-logs/{id}")
    public ActivityLog getAuditLog(@PathVariable Long id) {
        ActivityLog log = activityLogMapper.selectById(id);
        if (log == null) throw new RuntimeException("Audit log not found: " + id);
        return log;
    }

    /** 写入活动日志（供内部服务调用）。 */
    @PostMapping("/api/projects/{projectId}/activity-logs")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityLog createActivityLog(@PathVariable Long projectId,
                                         @RequestBody ActivityLog body) {
        body.setProjectId(projectId);
        if (body.getOccurredAt() == null) body.setOccurredAt(java.time.LocalDateTime.now());
        activityLogMapper.insert(body);
        return body;
    }
}
