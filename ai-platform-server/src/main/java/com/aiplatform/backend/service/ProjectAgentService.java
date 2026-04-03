package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.UpdateProjectAgentRequest;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectAgent;
import com.aiplatform.backend.mapper.ProjectAgentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Service for managing the dedicated agent bound to a project.
 */
@Service
public class ProjectAgentService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String DEFAULT_AGENT_NAME_SUFFIX = " Assistant";
    private static final String DEFAULT_AGENT_AVATAR = "bot";
    private static final String DEFAULT_TEXT_FALLBACK = "-";

    private final ProjectAgentMapper projectAgentMapper;

    public ProjectAgentService(ProjectAgentMapper projectAgentMapper) {
        this.projectAgentMapper = projectAgentMapper;
    }

    public ProjectAgent initForProject(Project project, Long createdBy) {
        ProjectAgent agent = new ProjectAgent();
        agent.setProjectId(project.getId());
        agent.setName(project.getName() + DEFAULT_AGENT_NAME_SUFFIX);
        agent.setDescription(buildDefaultDescription(project));
        agent.setAvatarIcon(DEFAULT_AGENT_AVATAR);
        agent.setSystemPrompt(buildDefaultSystemPrompt(project));
        agent.setPreferredModel(null);
        agent.setEnableRag(true);
        agent.setEnableSkills(true);
        agent.setEnableTools(true);
        agent.setEnableDeploy(false);
        agent.setEnableMonitoring(false);
        agent.setStatus(STATUS_ACTIVE);
        agent.setCreatedBy(createdBy);
        projectAgentMapper.insert(agent);
        return agent;
    }

    public ProjectAgent getByProjectIdOrThrow(Long projectId) {
        ProjectAgent agent = projectAgentMapper.selectOne(
                Wrappers.<ProjectAgent>lambdaQuery()
                        .eq(ProjectAgent::getProjectId, projectId)
        );
        if (agent == null) {
            throw new AgentNotFoundException(projectId);
        }
        return agent;
    }

    public ProjectAgent update(Long projectId, UpdateProjectAgentRequest request) {
        ProjectAgent agent = getByProjectIdOrThrow(projectId);
        applyTextUpdate(request.name(), agent::setName);
        applyTextUpdate(request.description(), agent::setDescription);
        applyTextUpdate(request.avatarIcon(), agent::setAvatarIcon);
        applyTextUpdate(request.systemPrompt(), agent::setSystemPrompt);
        applyTextUpdate(request.preferredModel(), agent::setPreferredModel);
        applyTextUpdate(request.status(), agent::setStatus);

        if (request.enableRag() != null) {
            agent.setEnableRag(request.enableRag());
        }
        if (request.enableSkills() != null) {
            agent.setEnableSkills(request.enableSkills());
        }
        if (request.enableTools() != null) {
            agent.setEnableTools(request.enableTools());
        }
        if (request.enableDeploy() != null) {
            agent.setEnableDeploy(request.enableDeploy());
        }
        if (request.enableMonitoring() != null) {
            agent.setEnableMonitoring(request.enableMonitoring());
        }
        projectAgentMapper.updateById(agent);
        return agent;
    }

    public ProjectAgent rebuildSystemPrompt(Project project) {
        ProjectAgent agent = getByProjectIdOrThrow(project.getId());
        agent.setSystemPrompt(buildDefaultSystemPrompt(project));
        projectAgentMapper.updateById(agent);
        return agent;
    }

    private void applyTextUpdate(String value, Consumer<String> setter) {
        String normalized = normalizeText(value);
        if (normalized != null) {
            setter.accept(normalized);
        }
    }

    private String buildDefaultDescription(Project project) {
        return "Dedicated AI assistant for project " + defaultIfBlank(project.getName(), DEFAULT_TEXT_FALLBACK);
    }

    private String buildDefaultSystemPrompt(Project project) {
        return """
                You are the dedicated AI assistant for project %s.

                ## Project Overview
                - Name: %s
                - Code: %s
                - Type: %s
                - Description: %s

                ## Responsibilities
                1. Help with requirements analysis, implementation questions, and technical troubleshooting.
                2. Keep answers grounded in the current project context and avoid generic advice.
                3. Surface assumptions, risks, and missing information explicitly before making risky suggestions.
                4. Prefer concise, actionable responses with clear next steps when helpful.
                5. Maintain engineering quality, delivery efficiency, and operational awareness.

                ## Guardrails
                - Do not fabricate project facts that are not provided.
                - Ask for the missing context when the request cannot be answered safely.
                - Keep recommendations aligned with the project's code, environment, and delivery process.
                """.formatted(
                defaultIfBlank(project.getName(), DEFAULT_TEXT_FALLBACK),
                defaultIfBlank(project.getName(), DEFAULT_TEXT_FALLBACK),
                defaultIfBlank(project.getCode(), DEFAULT_TEXT_FALLBACK),
                defaultIfBlank(project.getProjectType(), DEFAULT_TEXT_FALLBACK),
                defaultIfBlank(project.getDescription(), DEFAULT_TEXT_FALLBACK)
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String normalized = normalizeText(value);
        return normalized != null ? normalized : defaultValue;
    }

    public static class AgentNotFoundException extends BusinessException {
        public AgentNotFoundException(Long projectId) {
            super(
                    HttpStatus.NOT_FOUND.value(),
                    BizErrorCode.PROJECT_AGENT_NOT_FOUND,
                    "Project agent not found: projectId=" + projectId
            );
        }
    }
}
