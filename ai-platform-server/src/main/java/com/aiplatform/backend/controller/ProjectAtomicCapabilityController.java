package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.ProjectAtomicSubscriptionResponse;
import com.aiplatform.backend.dto.SubscribeAtomicCapabilityRequest;
import com.aiplatform.backend.service.ProjectAtomicCapabilityService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/projects/{projectId}/atomic-capabilities")
public class ProjectAtomicCapabilityController {

    private final ProjectAtomicCapabilityService projectAtomicCapabilityService;

    public ProjectAtomicCapabilityController(ProjectAtomicCapabilityService projectAtomicCapabilityService) {
        this.projectAtomicCapabilityService = projectAtomicCapabilityService;
    }

    @GetMapping
    public List<ProjectAtomicSubscriptionResponse> list(@PathVariable Long projectId) {
        return projectAtomicCapabilityService.listByProjectId(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectAtomicSubscriptionResponse subscribe(@PathVariable Long projectId,
                                                       @Valid @RequestBody SubscribeAtomicCapabilityRequest request) {
        return projectAtomicCapabilityService.subscribe(projectId, request.capabilityId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long projectId, @PathVariable Long id) {
        projectAtomicCapabilityService.unsubscribe(projectId, id);
    }
}
