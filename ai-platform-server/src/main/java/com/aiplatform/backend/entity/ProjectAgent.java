package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 项目专属智能体实体，对应 {@code project_agents} 表。
 *
 * <p>每个项目在创建时自动生成一个专属智能体。
 * 智能体聚合了项目关联的技能 System Prompt、知识库 RAG、工具调用能力，
 * 可回答「今天项目有什么 Bug」「帮我发布到生产」等项目相关问题。</p>
 */
@TableName("project_agents")
public class ProjectAgent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目 ID（唯一） */
    private Long projectId;

    /** 智能体名称，默认「{项目名}助手」 */
    private String name;

    /** 智能体描述 */
    private String description;

    /** 头像图标（emoji 或 icon name） */
    private String avatarIcon;

    /** 项目智能体全局 System Prompt（由平台默认生成，管理员可覆盖） */
    private String systemPrompt;

    /** 优先使用的模型代码，如 {@code gpt-4o}，null 表示使用平台默认 */
    private String preferredModel;

    /** 是否启用知识库 RAG 增强 */
    private Boolean enableRag;

    /** 是否启用项目技能注入 */
    private Boolean enableSkills;

    /** 是否启用工具调用 */
    private Boolean enableTools;

    /** 是否启用部署指令能力（「帮我发布到生产」等） */
    private Boolean enableDeploy;

    /** 是否启用监控告警查询能力 */
    private Boolean enableMonitoring;

    /** 状态：ACTIVE / DISABLED */
    private String status;

    /** 创建者用户 ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAvatarIcon() { return avatarIcon; }
    public void setAvatarIcon(String avatarIcon) { this.avatarIcon = avatarIcon; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public String getPreferredModel() { return preferredModel; }
    public void setPreferredModel(String preferredModel) { this.preferredModel = preferredModel; }

    public Boolean getEnableRag() { return enableRag; }
    public void setEnableRag(Boolean enableRag) { this.enableRag = enableRag; }

    public Boolean getEnableSkills() { return enableSkills; }
    public void setEnableSkills(Boolean enableSkills) { this.enableSkills = enableSkills; }

    public Boolean getEnableTools() { return enableTools; }
    public void setEnableTools(Boolean enableTools) { this.enableTools = enableTools; }

    public Boolean getEnableDeploy() { return enableDeploy; }
    public void setEnableDeploy(Boolean enableDeploy) { this.enableDeploy = enableDeploy; }

    public Boolean getEnableMonitoring() { return enableMonitoring; }
    public void setEnableMonitoring(Boolean enableMonitoring) { this.enableMonitoring = enableMonitoring; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
