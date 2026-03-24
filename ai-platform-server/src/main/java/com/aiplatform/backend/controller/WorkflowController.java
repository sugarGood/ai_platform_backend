package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.WorkflowDefinition;
import com.aiplatform.backend.mapper.WorkflowDefinitionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 工作流引擎控制器（模块17）。 */
@RestController
@RequestMapping("/api/projects/{projectId}/workflows")
public class WorkflowController {

    private final WorkflowDefinitionMapper mapper;

    public WorkflowController(WorkflowDefinitionMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public List<WorkflowDefinition> list(@PathVariable Long projectId) {
        return mapper.selectList(Wrappers.<WorkflowDefinition>lambdaQuery()
                .eq(WorkflowDefinition::getProjectId, projectId)
                .orderByDesc(WorkflowDefinition::getId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowDefinition create(@PathVariable Long projectId,
                                     @RequestBody WorkflowDefinition body) {
        body.setProjectId(projectId);
        if (body.getStatus() == null) body.setStatus("DRAFT");
        mapper.insert(body);
        return body;
    }

    @GetMapping("/{id}")
    public WorkflowDefinition getById(@PathVariable Long projectId, @PathVariable Long id) {
        WorkflowDefinition e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("Workflow not found: " + id);
        return e;
    }

    @PutMapping("/{id}")
    public WorkflowDefinition update(@PathVariable Long projectId,
                                     @PathVariable Long id,
                                     @RequestBody WorkflowDefinition body) {
        WorkflowDefinition e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("Workflow not found: " + id);
        if (body.getName() != null) e.setName(body.getName());
        if (body.getDefinitionJson() != null) e.setDefinitionJson(body.getDefinitionJson());
        mapper.updateById(e);
        return e;
    }

    @PostMapping("/{id}/publish")
    public WorkflowDefinition publish(@PathVariable Long projectId, @PathVariable Long id) {
        WorkflowDefinition e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("Workflow not found: " + id);
        e.setStatus("PUBLISHED");
        mapper.updateById(e);
        return e;
    }

    @PostMapping("/{id}/archive")
    public WorkflowDefinition archive(@PathVariable Long projectId, @PathVariable Long id) {
        WorkflowDefinition e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("Workflow not found: " + id);
        e.setStatus("ARCHIVED");
        mapper.updateById(e);
        return e;
    }

    /** 手动触发工作流执行。TODO: 接入工作流执行引擎。 */
    @PostMapping("/{id}/execute")
    public Map<String, Object> execute(@PathVariable Long projectId, @PathVariable Long id,
                                       @RequestBody(required = false) Map<String, Object> input) {
        mapper.selectById(id);
        return Map.of("workflowId", id, "status", "PENDING", "message", "工作流执行引擎待集成");
    }
}
