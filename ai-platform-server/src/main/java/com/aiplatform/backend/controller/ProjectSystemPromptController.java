package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.ProjectSystemPrompt;
import com.aiplatform.backend.mapper.ProjectSystemPromptMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 项目 System Prompt 管理控制器（模块16）。
 */
@RestController
@RequestMapping("/api/projects/{projectId}/system-prompts")
public class ProjectSystemPromptController {

    private final ProjectSystemPromptMapper mapper;

    public ProjectSystemPromptController(ProjectSystemPromptMapper mapper) {
        this.mapper = mapper;
    }

    /** 查询项目 Prompt 列表（按优先级排序）。 */
    @GetMapping
    public List<ProjectSystemPrompt> list(@PathVariable Long projectId) {
        return mapper.selectList(Wrappers.<ProjectSystemPrompt>lambdaQuery()
                .eq(ProjectSystemPrompt::getProjectId, projectId)
                .orderByAsc(ProjectSystemPrompt::getPriority));
    }

    /** 创建 Prompt。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectSystemPrompt create(@PathVariable Long projectId,
                                      @RequestBody ProjectSystemPrompt body) {
        body.setProjectId(projectId);
        if (body.getStatus() == null) body.setStatus("ACTIVE");
        if (body.getInjectStrategy() == null) body.setInjectStrategy("ALWAYS");
        if (body.getPriority() == null) body.setPriority(0);
        mapper.insert(body);
        return body;
    }

    /** 编辑 Prompt。 */
    @PutMapping("/{id}")
    public ProjectSystemPrompt update(@PathVariable Long projectId,
                                      @PathVariable Long id,
                                      @RequestBody ProjectSystemPrompt body) {
        ProjectSystemPrompt existing = mapper.selectById(id);
        if (existing == null || !existing.getProjectId().equals(projectId))
            throw new RuntimeException("SystemPrompt not found: " + id);
        if (body.getPromptName() != null) existing.setPromptName(body.getPromptName());
        if (body.getContent() != null) existing.setContent(body.getContent());
        if (body.getInjectStrategy() != null) existing.setInjectStrategy(body.getInjectStrategy());
        if (body.getMaxTokens() != null) existing.setMaxTokens(body.getMaxTokens());
        if (body.getPriority() != null) existing.setPriority(body.getPriority());
        mapper.updateById(existing);
        return existing;
    }

    /** 删除 Prompt。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId, @PathVariable Long id) {
        mapper.delete(Wrappers.<ProjectSystemPrompt>lambdaQuery()
                .eq(ProjectSystemPrompt::getProjectId, projectId)
                .eq(ProjectSystemPrompt::getId, id));
    }

    /** 调整优先级。 */
    @PatchMapping("/{id}/priority")
    public ProjectSystemPrompt updatePriority(@PathVariable Long projectId,
                                              @PathVariable Long id,
                                              @RequestBody Map<String, Integer> body) {
        ProjectSystemPrompt existing = mapper.selectById(id);
        if (existing == null || !existing.getProjectId().equals(projectId)) {
            throw new RuntimeException("SystemPrompt not found: " + id);
        }
        existing.setPriority(body.getOrDefault("priority", existing.getPriority()));
        mapper.updateById(existing);
        return existing;
    }
}
