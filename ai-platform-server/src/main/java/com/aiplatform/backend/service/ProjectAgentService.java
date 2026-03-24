package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.UpdateProjectAgentRequest;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectAgent;
import com.aiplatform.backend.mapper.ProjectAgentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 项目专属智能体业务服务。
 *
 * <p>负责：
 * <ul>
 *   <li>项目创建时自动初始化智能体（{@link #initForProject}）</li>
 *   <li>查询项目智能体配置</li>
 *   <li>管理员更新智能体配置</li>
 *   <li>手动重建智能体 System Prompt</li>
 * </ul>
 * </p>
 */
@Service
public class ProjectAgentService {

    private final ProjectAgentMapper projectAgentMapper;

    public ProjectAgentService(ProjectAgentMapper projectAgentMapper) {
        this.projectAgentMapper = projectAgentMapper;
    }

    // ---------------------------------------------------------------
    // 项目创建时自动初始化
    // ---------------------------------------------------------------

    /**
     * 为新建项目自动创建专属智能体。
     *
     * <p>由 {@link ProjectService#create} 在事务提交前调用。
     * 生成默认名称、System Prompt，并启用 RAG 和技能注入。</p>
     *
     * @param project   已持久化的项目实体
     * @param createdBy 创建人用户 ID（可为 null）
     */
    public ProjectAgent initForProject(Project project, Long createdBy) {
        ProjectAgent agent = new ProjectAgent();
        agent.setProjectId(project.getId());
        agent.setName(project.getName() + " · 乐知");
        agent.setDescription("乐知是 " + project.getName() + " 项目的专属 AI 助手，乐于求知、乐于解答。负责回答项目相关的所有问题，包括 Bug 排查、发布运维、文档查询等。");
        agent.setAvatarIcon("🔮");
        agent.setSystemPrompt(buildDefaultSystemPrompt(project));
        agent.setPreferredModel(null);     // 使用平台默认模型
        agent.setEnableRag(true);
        agent.setEnableSkills(true);
        agent.setEnableTools(true);
        agent.setEnableDeploy(false);      // 部署能力默认关闭，需管理员手动开启
        agent.setEnableMonitoring(false);  // 监控能力默认关闭，需管理员手动开启
        agent.setStatus("ACTIVE");
        agent.setCreatedBy(createdBy);
        projectAgentMapper.insert(agent);
        return agent;
    }

    // ---------------------------------------------------------------
    // 查询
    // ---------------------------------------------------------------

    /**
     * 根据项目 ID 获取专属智能体，不存在时抛出 {@link AgentNotFoundException}。
     *
     * @param projectId 项目 ID
     * @return 项目智能体实体
     */
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

    // ---------------------------------------------------------------
    // 更新
    // ---------------------------------------------------------------

    /**
     * 更新项目智能体配置（仅更新请求中非 null 的字段）。
     *
     * @param projectId 项目 ID
     * @param request   更新请求
     * @return 更新后的智能体实体
     */
    public ProjectAgent update(Long projectId, UpdateProjectAgentRequest request) {
        ProjectAgent agent = getByProjectIdOrThrow(projectId);
        if (request.name() != null)              agent.setName(request.name());
        if (request.description() != null)       agent.setDescription(request.description());
        if (request.avatarIcon() != null)        agent.setAvatarIcon(request.avatarIcon());
        if (request.systemPrompt() != null)      agent.setSystemPrompt(request.systemPrompt());
        if (request.preferredModel() != null)    agent.setPreferredModel(request.preferredModel());
        if (request.enableRag() != null)         agent.setEnableRag(request.enableRag());
        if (request.enableSkills() != null)      agent.setEnableSkills(request.enableSkills());
        if (request.enableTools() != null)       agent.setEnableTools(request.enableTools());
        if (request.enableDeploy() != null)      agent.setEnableDeploy(request.enableDeploy());
        if (request.enableMonitoring() != null)  agent.setEnableMonitoring(request.enableMonitoring());
        if (request.status() != null)            agent.setStatus(request.status());
        projectAgentMapper.updateById(agent);
        return agent;
    }

    // ---------------------------------------------------------------
    // 重建 System Prompt
    // ---------------------------------------------------------------

    /**
     * 根据项目当前信息重建默认 System Prompt（覆盖已有自定义内容）。
     *
     * @param project 项目实体
     * @return 更新后的智能体实体
     */
    public ProjectAgent rebuildSystemPrompt(Project project) {
        ProjectAgent agent = getByProjectIdOrThrow(project.getId());
        agent.setSystemPrompt(buildDefaultSystemPrompt(project));
        projectAgentMapper.updateById(agent);
        return agent;
    }

    // ---------------------------------------------------------------
    // 内部工具
    // ---------------------------------------------------------------

    /**
     * 根据项目信息构建默认 System Prompt。
     *
     * <p>该 Prompt 会被注入到网关请求的最前面，为智能体提供项目背景知识。
     * 当 enableRag=true 时，网关还会追加 RAG 检索到的知识片段。
     * 当 enableSkills=true 时，网关还会追加项目技能的 System Prompt。</p>
     */
    private String buildDefaultSystemPrompt(Project project) {
        return String.format(
                """
                你是【%s】项目的专属 AI 助手「乐知」。

                ## 项目基本信息
                - 项目名称：%s
                - 项目编码：%s
                - 项目类型：%s
                - 项目描述：%s

                ## 你的职责
                你可以帮助项目成员解答以下问题：
                1. **Bug 排查**：分析项目日志、错误信息，给出诊断和修复建议
                2. **运行状态**：查询项目当前服务运行状态、告警信息
                3. **文档查询**：检索项目相关文档、规范、知识库内容
                4. **代码辅助**：基于项目技术栈提供代码建议和审查
                5. **发布管理**：了解当前部署状态，在授权后协助触发发布流程

                ## 回答规范
                - 回答应简洁、准确，聚焦于项目实际情况
                - 对于不确定的信息，主动说明并建议查阅具体文档或联系相关负责人
                - 涉及生产发布、配置变更等高风险操作，必须先确认操作人身份和意图
                """,
                project.getName(),
                project.getName(),
                project.getCode() != null ? project.getCode() : "-",
                project.getProjectType() != null ? project.getProjectType() : "-",
                project.getDescription() != null ? project.getDescription() : "-"
        );
    }

    // ---------------------------------------------------------------
    // 内部异常
    // ---------------------------------------------------------------

    public static class AgentNotFoundException extends BusinessException {
        public AgentNotFoundException(Long projectId) {
            super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PROJECT_AGENT_NOT_FOUND,
                    "项目智能体不存在，projectId=" + projectId);
        }
    }
}
