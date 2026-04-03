package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.IncidentStatusUpdateRequest;
import com.aiplatform.backend.entity.Incident;
import com.aiplatform.backend.mapper.IncidentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

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
        var query = Wrappers.<Incident>lambdaQuery().eq(Incident::getProjectId, projectId);
        if (status != null) {
            query.eq(Incident::getStatus, status);
        }
        return incidentMapper.selectList(query.orderByDesc(Incident::getId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Incident create(@PathVariable Long projectId, @RequestBody Incident body) {
        body.setProjectId(projectId);
        if (body.getStatus() == null) {
            body.setStatus("OPEN");
        }
        incidentMapper.insert(body);
        return body;
    }

    @GetMapping("/{id}")
    public Incident getById(@PathVariable Long projectId, @PathVariable Long id) {
        Incident incident = incidentMapper.selectById(id);
        if (incident == null || !projectId.equals(incident.getProjectId())) {
            throw new RuntimeException("Incident not found: " + id);
        }
        return incident;
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long projectId,
                                 @PathVariable Long id,
                                 @Valid @RequestBody IncidentStatusUpdateRequest request) {
        Incident incident = incidentMapper.selectById(id);
        if (incident == null || !projectId.equals(incident.getProjectId())) {
            throw new RuntimeException("Incident not found: " + id);
        }
        incident.setStatus(request.status().trim());
        if ("RESOLVED".equals(incident.getStatus())) {
            incident.setResolvedAt(LocalDateTime.now());
        }
        incidentMapper.updateById(incident);
        return incident;
    }

    @PostMapping("/{id}/ai-diagnose")
    public Incident aiDiagnose(@PathVariable Long projectId, @PathVariable Long id) {
        Incident incident = incidentMapper.selectById(id);
        if (incident == null || !projectId.equals(incident.getProjectId())) {
            throw new RuntimeException("Incident not found: " + id);
        }
        incident.setAiDiagnosisStatus("PENDING");
        incident.setAiDiagnosis("AI diagnosis pending. Preview: " + previewErrorStack(incident.getErrorStack()));
        incidentMapper.updateById(incident);
        return incident;
    }

    private String previewErrorStack(String errorStack) {
        if (errorStack == null || errorStack.isBlank()) {
            return "(empty)";
        }
        return errorStack.substring(0, Math.min(100, errorStack.length()));
    }
}
