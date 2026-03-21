package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateKnowledgeBaseRequest;
import com.aiplatform.backend.dto.KnowledgeBaseResponse;
import com.aiplatform.backend.entity.ProjectKnowledgeConfig;
import com.aiplatform.backend.service.KnowledgeBaseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目知识库配置控制器。
 *
 * <p>管理项目与全局知识库的关联配置，路径前缀为 {@code /api/projects/{projectId}/knowledge-configs}。</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/knowledge-configs")
public class ProjectKnowledgeConfigController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 构造函数，注入知识库业务服务。
     *
     * @param knowledgeBaseService 知识库业务服务
     */
    public ProjectKnowledgeConfigController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 为项目启用全局知识库。
     *
     * @param projectId    项目ID
     * @param kbId         全局知识库ID
     * @param searchWeight 检索权重（0~1），可选
     * @return 新创建的项目知识库配置
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectKnowledgeConfig enable(@PathVariable Long projectId,
                                         @RequestParam Long kbId,
                                         @RequestParam(required = false) BigDecimal searchWeight) {
        return knowledgeBaseService.enableForProject(projectId, kbId, searchWeight);
    }

    /**
     * 查询项目的知识库配置列表。
     *
     * @param projectId 项目ID
     * @return 项目知识库配置列表
     */
    @GetMapping
    public List<ProjectKnowledgeConfig> list(@PathVariable Long projectId) {
        return knowledgeBaseService.listProjectConfigs(projectId);
    }
}
