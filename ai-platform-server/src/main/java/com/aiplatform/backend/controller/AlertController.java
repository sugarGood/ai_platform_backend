package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.entity.AlertEvent;
import com.aiplatform.backend.entity.AlertRule;
import com.aiplatform.backend.mapper.AlertEventMapper;
import com.aiplatform.backend.mapper.AlertRuleMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 告警与事故管理控制器（模块14）。
 *
 * <p>提供告警规则、告警事件的完整 CRUD 接口。</p>
 */
@RestController
public class AlertController {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertEventMapper alertEventMapper;

    public AlertController(AlertRuleMapper alertRuleMapper, AlertEventMapper alertEventMapper) {
        this.alertRuleMapper = alertRuleMapper;
        this.alertEventMapper = alertEventMapper;
    }

    // ── 告警规则 ─────────────────────────────────────────────────────

    /** 查询告警规则列表（支持 scope/projectId 过滤）。 */
    @GetMapping("/api/alert-rules")
    public java.util.List<AlertRule> listRules(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Long projectId) {
        var q = Wrappers.<AlertRule>lambdaQuery();
        if (scope != null) q.eq(AlertRule::getScope, scope);
        if (projectId != null) q.eq(AlertRule::getProjectId, projectId);
        q.orderByDesc(AlertRule::getId);
        return alertRuleMapper.selectList(q);
    }

    /** 创建告警规则。 */
    @PostMapping("/api/alert-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public AlertRule createRule(@RequestBody AlertRule body) {
        if (body.getStatus() == null) body.setStatus("ACTIVE");
        alertRuleMapper.insert(body);
        return body;
    }

    /** 编辑告警规则。 */
    @PutMapping("/api/alert-rules/{id}")
    public AlertRule updateRule(@PathVariable Long id, @RequestBody AlertRule body) {
        AlertRule existing = alertRuleMapper.selectById(id);
        if (existing == null) throw new RuntimeException("AlertRule not found: " + id);
        if (body.getName() != null) existing.setName(body.getName());
        if (body.getTriggerCondition() != null) existing.setTriggerCondition(body.getTriggerCondition());
        if (body.getTriggerExpression() != null) existing.setTriggerExpression(body.getTriggerExpression());
        if (body.getSeverity() != null) existing.setSeverity(body.getSeverity());
        if (body.getCooldownMinutes() != null) existing.setCooldownMinutes(body.getCooldownMinutes());
        alertRuleMapper.updateById(existing);
        return existing;
    }

    /** 启用/禁用告警规则。 */
    @PatchMapping("/api/alert-rules/{id}/status")
    public AlertRule updateRuleStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        AlertRule existing = alertRuleMapper.selectById(id);
        if (existing == null) throw new RuntimeException("AlertRule not found: " + id);
        existing.setStatus(body.getOrDefault("status", existing.getStatus()));
        alertRuleMapper.updateById(existing);
        return existing;
    }

    // ── 告警事件 ─────────────────────────────────────────────────────

    /** 查询告警事件列表（分页，支持 status/severity/projectId 过滤）。 */
    @GetMapping("/api/alert-events")
    public PageResponse<AlertEvent> listEvents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = alertEventMapper.selectPageWithRule(new Page<>(page, size), status, severity, projectId);
        return PageResponse.from(result, e -> e);
    }

    /** 确认告警（FIRING → ACKNOWLEDGED）。 */
    @PostMapping("/api/alert-events/{id}/acknowledge")
    public AlertEvent acknowledge(@PathVariable Long id) {
        AlertEvent event = alertEventMapper.selectById(id);
        if (event == null) throw new RuntimeException("AlertEvent not found: " + id);
        event.setStatus("ACKNOWLEDGED");
        alertEventMapper.updateById(event);
        return event;
    }

    /** 解决告警（→ RESOLVED）。 */
    @PostMapping("/api/alert-events/{id}/resolve")
    public AlertEvent resolve(@PathVariable Long id) {
        AlertEvent event = alertEventMapper.selectById(id);
        if (event == null) throw new RuntimeException("AlertEvent not found: " + id);
        event.setStatus("RESOLVED");
        event.setResolvedAt(LocalDateTime.now());
        alertEventMapper.updateById(event);
        return event;
    }
}
