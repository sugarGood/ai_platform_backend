package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.EnableProjectKnowledgeConfigRequest;
import com.aiplatform.backend.dto.UpdateProjectKnowledgeConfigRequest;
import com.aiplatform.backend.entity.ProjectKnowledgeConfig;
import com.aiplatform.backend.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/knowledge-configs")
public class ProjectKnowledgeConfigController {

    private final KnowledgeBaseService knowledgeBaseService;

    public ProjectKnowledgeConfigController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 绑定知识库到项目。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectKnowledgeConfig enable(@PathVariable Long projectId,
                                         @Valid @RequestBody EnableProjectKnowledgeConfigRequest request) {
        return knowledgeBaseService.enableForProject(projectId, request);
    }

    /**
     * 获取项目知识库绑定配置列表。
     */
    @GetMapping
    public List<ProjectKnowledgeConfig> list(@PathVariable Long projectId) {
        return knowledgeBaseService.listProjectConfigs(projectId);
    }

    /**
     * 更新项目知识库绑定配置。
     */
    @PutMapping("/{id}")
    public ProjectKnowledgeConfig update(@PathVariable Long projectId,
                                         @PathVariable Long id,
                                         @Valid @RequestBody UpdateProjectKnowledgeConfigRequest request) {
        return knowledgeBaseService.updateProjectKnowledgeConfig(projectId, id, request);
    }

    /**
     * 移除项目知识库绑定配置。
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable Long projectId, @PathVariable Long id) {
        knowledgeBaseService.disableProjectConfig(projectId, id);
    }
}
