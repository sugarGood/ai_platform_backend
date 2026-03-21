package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 技能定义实体，对应 skills 表。
 *
 * <p>技能是平台 AI 能力编排的核心单元，定义了角色 Prompt、关联知识库、绑定工具等配置。
 * 支持通过斜杠命令触发，并提供使用统计和满意度反馈追踪。</p>
 */
@TableName("skills")
public class Skill {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 技能唯一标识（英文标识符） */
    private String skillKey;

    /** 技能名称 */
    private String name;

    /** 技能描述 */
    private String description;

    /** 作用域：GLOBAL（全局）/ PROJECT（项目级） */
    private String scope;

    /** 所属项目ID，scope 为 PROJECT 时有效 */
    private Long projectId;

    /** 技能分类：ENGINEERING / QUALITY / SECURITY 等 */
    private String category;

    /** 角色 Prompt 模板 */
    private String systemPrompt;

    /** 关联知识库引用（JSON 格式） */
    private String knowledgeRefs;

    /** 绑定工具列表（JSON 格式） */
    private String boundTools;

    /** 用户可配参数（JSON 格式） */
    private String parameters;

    /** 斜杠触发命令，如 /code-review */
    private String slashCommand;

    /** 版本号 */
    private String version;

    /** 发布状态：DRAFT（草稿）/ PUBLISHED（已发布）/ DEPRECATED（已弃用） */
    private String status;

    /** 累计使用次数 */
    private Long usageCount;

    /** 正反馈数（点赞） */
    private Integer satisfactionUp;

    /** 负反馈数（点踩） */
    private Integer satisfactionDown;

    /** 创建者用户ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSkillKey() { return skillKey; }
    public void setSkillKey(String skillKey) { this.skillKey = skillKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getKnowledgeRefs() { return knowledgeRefs; }
    public void setKnowledgeRefs(String knowledgeRefs) { this.knowledgeRefs = knowledgeRefs; }
    public String getBoundTools() { return boundTools; }
    public void setBoundTools(String boundTools) { this.boundTools = boundTools; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public String getSlashCommand() { return slashCommand; }
    public void setSlashCommand(String slashCommand) { this.slashCommand = slashCommand; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    public Integer getSatisfactionUp() { return satisfactionUp; }
    public void setSatisfactionUp(Integer satisfactionUp) { this.satisfactionUp = satisfactionUp; }
    public Integer getSatisfactionDown() { return satisfactionDown; }
    public void setSatisfactionDown(Integer satisfactionDown) { this.satisfactionDown = satisfactionDown; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
