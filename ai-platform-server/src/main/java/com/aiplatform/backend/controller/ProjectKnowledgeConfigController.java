package com.aiplatform.backend.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目知识库配置控制器（完整版）。
 *
 * <p>管理项目与全局知识库的关联配置，路径前缀为 {@code /api/projects/{projectId}/knowledge-configs}。</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/knowledge-configs")
public class ProjectKnowledgeConfigController {

    private final KnowledgeBaseService knowledgeBaseService;

    public ProjectKnowledgeConfigController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /** 为项目启用全局知识库。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectKnowledgeConfig enable(@PathVariable Long projectId,
                                         @RequestParam Long kbId,
                                         @RequestParam(required = false) BigDecimal searchWeight,
                                         @RequestParam(required = false) String injectMode) {
        return knowledgeBaseService.enableForProject(projectId, kbId, searchWeight, injectMode);
    }

    /** 查询项目的知识库配置列表。 */
    @GetMapping
    public List<ProjectKnowledgeConfig> list(@PathVariable Long projectId) {
        return knowledgeBaseService.listProjectConfigs(projectId);
    }

    /** 修改项目知识库绑定配置（权重、注入方式、状态等，仅更新请求体中提供的字段）。 */
    @PutMapping("/{id}")
    public ProjectKnowledgeConfig update(@PathVariable Long projectId,
                                         @PathVariable Long id,
                                         @Valid @RequestBody UpdateProjectKnowledgeConfigRequest request) {
        return knowledgeBaseService.updateProjectKnowledgeConfig(projectId, id, request);
    }

    /** 解绑项目知识库配置。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable Long projectId, @PathVariable Long id) {
        knowledgeBaseService.disableProjectConfig(projectId, id);
    }
}
