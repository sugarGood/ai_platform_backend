package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.AlertRuleStatusUpdateRequest;
import com.aiplatform.backend.entity.AlertEvent;
import com.aiplatform.backend.entity.AlertRule;
import com.aiplatform.backend.mapper.AlertEventMapper;
import com.aiplatform.backend.mapper.AlertRuleMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
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
import java.util.List;

@RestController
public class AlertController {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertEventMapper alertEventMapper;

    public AlertController(AlertRuleMapper alertRuleMapper, AlertEventMapper alertEventMapper) {
        this.alertRuleMapper = alertRuleMapper;
        this.alertEventMapper = alertEventMapper;
    }

    @GetMapping("/api/alert-rules")
    public List<AlertRule> listRules(@RequestParam(required = false) String scope,
                                     @RequestParam(required = false) Long projectId) {
        var query = Wrappers.<AlertRule>lambdaQuery();
        if (scope != null) {
            query.eq(AlertRule::getScope, scope);
        }
        if (projectId != null) {
            query.eq(AlertRule::getProjectId, projectId);
        }
        query.orderByDesc(AlertRule::getId);
        return alertRuleMapper.selectList(query);
    }

    @PostMapping("/api/alert-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public AlertRule createRule(@RequestBody AlertRule body) {
        if (body.getStatus() == null) {
            body.setStatus("ACTIVE");
        }
        alertRuleMapper.insert(body);
        return body;
    }

    @PutMapping("/api/alert-rules/{id}")
    public AlertRule updateRule(@PathVariable Long id, @RequestBody AlertRule body) {
        AlertRule existing = alertRuleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("AlertRule not found: " + id);
        }
        if (body.getName() != null) {
            existing.setName(body.getName());
        }
        if (body.getTriggerCondition() != null) {
            existing.setTriggerCondition(body.getTriggerCondition());
        }
        if (body.getTriggerExpression() != null) {
            existing.setTriggerExpression(body.getTriggerExpression());
        }
        if (body.getSeverity() != null) {
            existing.setSeverity(body.getSeverity());
        }
        if (body.getCooldownMinutes() != null) {
            existing.setCooldownMinutes(body.getCooldownMinutes());
        }
        alertRuleMapper.updateById(existing);
        return existing;
    }

    @PatchMapping("/api/alert-rules/{id}/status")
    public AlertRule updateRuleStatus(@PathVariable Long id,
                                      @Valid @RequestBody AlertRuleStatusUpdateRequest request) {
        AlertRule existing = alertRuleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("AlertRule not found: " + id);
        }
        existing.setStatus(request.status().trim());
        alertRuleMapper.updateById(existing);
        return existing;
    }

    @GetMapping("/api/alert-events")
    public PageResponse<AlertEvent> listEvents(@RequestParam(required = false) String status,
                                               @RequestParam(required = false) String severity,
                                               @RequestParam(required = false) Long projectId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        var result = alertEventMapper.selectPageWithRule(new Page<>(page, size), status, severity, projectId);
        return PageResponse.from(result, event -> event);
    }

    @PostMapping("/api/alert-events/{id}/acknowledge")
    public AlertEvent acknowledge(@PathVariable Long id) {
        AlertEvent event = alertEventMapper.selectById(id);
        if (event == null) {
            throw new RuntimeException("AlertEvent not found: " + id);
        }
        event.setStatus("ACKNOWLEDGED");
        alertEventMapper.updateById(event);
        return event;
    }

    @PostMapping("/api/alert-events/{id}/resolve")
    public AlertEvent resolve(@PathVariable Long id) {
        AlertEvent event = alertEventMapper.selectById(id);
        if (event == null) {
            throw new RuntimeException("AlertEvent not found: " + id);
        }
        event.setStatus("RESOLVED");
        event.setResolvedAt(LocalDateTime.now());
        alertEventMapper.updateById(event);
        return event;
    }
}
