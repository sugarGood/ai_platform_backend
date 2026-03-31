package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.ProjectKnowledgeSourceFilter;
import com.aiplatform.backend.dto.ProjectKnowledgeSourcesResponse;
import com.aiplatform.backend.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目维度知识库查询：区分「项目专属」与「继承的全局库」。
 */
@RestController
@RequestMapping("/api/projects/{projectId}/knowledge-sources")
public class ProjectKnowledgeSourceController {

    private final KnowledgeBaseService knowledgeBaseService;

    public ProjectKnowledgeSourceController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 查询项目下知识库来源，统一为 {@code items} 列表；{@link com.aiplatform.backend.dto.ProjectKnowledgeSourceItem#kind}
     * 区分 {@code DEDICATED}（项目专属）与 {@code GLOBAL_INHERITED}（继承的全局库）。
     *
     * @param source 可选筛选：{@code all}（默认）、{@code dedicated} 独有、{@code global} 继承的全局库；
     *               亦接受 {@code project} / {@code exclusive}、{@code inherited}
     */
    @GetMapping
    public ProjectKnowledgeSourcesResponse list(
            @PathVariable Long projectId,
            @RequestParam(required = false) String source) {
        return knowledgeBaseService.listKnowledgeSourcesForProject(projectId, parseSource(source));
    }

    private static ProjectKnowledgeSourceFilter parseSource(String raw) {
        if (raw == null || raw.isBlank()) {
            return ProjectKnowledgeSourceFilter.ALL;
        }
        return switch (raw.trim().toLowerCase()) {
            case "all" -> ProjectKnowledgeSourceFilter.ALL;
            case "dedicated", "project", "exclusive", "独有" -> ProjectKnowledgeSourceFilter.DEDICATED;
            case "global", "inherited", "全局" -> ProjectKnowledgeSourceFilter.GLOBAL_INHERITED;
            default -> throw new BusinessException(400, "INVALID_KB_SOURCE_FILTER",
                    "source 无效，请使用 all、dedicated（独有）或 global（继承的全局库）");
        };
    }
}
