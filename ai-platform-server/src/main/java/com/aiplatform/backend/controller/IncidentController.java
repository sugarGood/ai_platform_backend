package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.Incident;
import com.aiplatform.backend.mapper.IncidentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 事故管理控制器（模块14扩展）。 */
@RestController
@RequestMapping("/api/projects/{projectId}/incidents")
public class IncidentController {

    private final IncidentMapper incidentMapper;

    public IncidentController(IncidentMapper incidentMapper) {
        this.incidentMapper = incidentMapper;
    }

    @GetMapping
    public List<Incident> list(@PathVariable Long projectId,
                               @RequestParam(required = false) String status) {
        var q = Wrappers.<Incident>lambdaQuery().eq(Incident::getProjectId, projectId);
        if (status != null) q.eq(Incident::getStatus, status);
        return incidentMapper.selectList(q.orderByDesc(Incident::getId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Incident create(@PathVariable Long projectId, @RequestBody Incident body) {
        body.setProjectId(projectId);
        if (body.getStatus() == null) body.setStatus("OPEN");
        incidentMapper.insert(body);
        return body;
    }

    @GetMapping("/{id}")
    public Incident getById(@PathVariable Long projectId, @PathVariable Long id) {
        Incident e = incidentMapper.selectById(id);
        if (e == null || !projectId.equals(e.getProjectId())) {
            throw new RuntimeException("Incident not found: " + id);
        }
        return e;
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long projectId,
                                 @PathVariable Long id,
                                 @RequestBody Map<String, String> body) {
        Incident e = incidentMapper.selectById(id);
        if (e == null || !projectId.equals(e.getProjectId())) {
            throw new RuntimeException("Incident not found: " + id);
        }
        e.setStatus(body.getOrDefault("status", e.getStatus()));
        if ("RESOLVED".equals(e.getStatus())) e.setResolvedAt(LocalDateTime.now());
        incidentMapper.updateById(e);
        return e;
    }

    /** AI 诊断事故（将 errorStack 发送给 AI 分析）。TODO: 接入 AI 网关。 */
    @PostMapping("/{id}/ai-diagnose")
    public Incident aiDiagnose(@PathVariable Long projectId, @PathVariable Long id) {
        Incident e = incidentMapper.selectById(id);
        if (e == null || !projectId.equals(e.getProjectId())) {
            throw new RuntimeException("Incident not found: " + id);
        }
        e.setAiDiagnosisStatus("PENDING");
        e.setAiDiagnosis("AI诊断引擎待集成，将分析以下错误栈: " +
                (e.getErrorStack() != null ? e.getErrorStack().substring(0, Math.min(100, e.getErrorStack().length())) : "(无)"));
        incidentMapper.updateById(e);
        return e;
    }
}
