package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.ProjectAtomicSubscriptionResponse;
import com.aiplatform.backend.service.ProjectAtomicCapabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 项目原子能力订阅控制器。 */
@RestController
@RequestMapping("/api/projects/{projectId}/atomic-capabilities")
public class ProjectAtomicCapabilityController {

    private final ProjectAtomicCapabilityService projectAtomicCapabilityService;

    public ProjectAtomicCapabilityController(ProjectAtomicCapabilityService projectAtomicCapabilityService) {
        this.projectAtomicCapabilityService = projectAtomicCapabilityService;
    }

    /** 查询项目已订阅的原子能力列表（含能力摘要）。 */
    @GetMapping
    public List<ProjectAtomicSubscriptionResponse> list(@PathVariable Long projectId) {
        return projectAtomicCapabilityService.listByProjectId(projectId);
    }

    /**
     * 项目订阅原子能力。
     *
     * <p>请求体：{@code { "capabilityId": &lt;long&gt; }}。已存在且为 DISABLED 时改为 ACTIVE。</p>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectAtomicSubscriptionResponse subscribe(@PathVariable Long projectId,
                                                       @RequestBody Map<String, Long> body) {
        Long capabilityId = body.get("capabilityId");
        if (capabilityId == null) {
            throw new IllegalArgumentException("capabilityId is required");
        }
        return projectAtomicCapabilityService.subscribe(projectId, capabilityId);
    }

    /**
     * 取消订阅。
     *
     * @param id {@code project_atomic_capabilities.id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long projectId, @PathVariable Long id) {
        projectAtomicCapabilityService.unsubscribe(projectId, id);
    }
}
