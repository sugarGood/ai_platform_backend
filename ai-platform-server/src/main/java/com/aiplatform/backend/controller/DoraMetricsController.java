package com.aiplatform.backend.controller;

import com.aiplatform.backend.mapper.AiUsageEventMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DORA 指标控制器（模块19）。
 * TODO: 接入 dora_metrics 表，此处从 ai_usage_events 实时聚合基础指标。
 */
@RestController
public class DoraMetricsController {

    private final AiUsageEventMapper aiUsageEventMapper;

    public DoraMetricsController(AiUsageEventMapper aiUsageEventMapper) {
        this.aiUsageEventMapper = aiUsageEventMapper;
    }

    /** 查询项目 DORA 指标。 */
    @GetMapping("/api/projects/{projectId}/dora-metrics")
    public Map<String, Object> projectDoraMetrics(
            @PathVariable Long projectId,
            @RequestParam(required = false) String period) {
        long totalRequests = aiUsageEventMapper.selectCount(
                Wrappers.<com.aiplatform.backend.entity.AiUsageEvent>lambdaQuery()
                        .eq(com.aiplatform.backend.entity.AiUsageEvent::getProjectId, projectId));
        return Map.of(
                "projectId", projectId,
                "period", period != null ? period : "CURRENT_MONTH",
                "deploymentFrequency", 0,
                "leadTimeForChanges", 0,
                "changeFailureRate", 0.0,
                "meanTimeToRestore", 0,
                "totalAiRequests", totalRequests,
                "message", "DORA指标计算引擎待集成"
        );
    }

    /** 平台 DORA 看板。 */
    @GetMapping("/api/admin/dora-dashboard")
    public Map<String, Object> platformDoraDashboard() {
        return Map.of(
                "deploymentFrequency", 0,
                "leadTimeForChanges", 0,
                "changeFailureRate", 0.0,
                "meanTimeToRestore", 0,
                "message", "DORA平台看板待集成"
        );
    }
}
