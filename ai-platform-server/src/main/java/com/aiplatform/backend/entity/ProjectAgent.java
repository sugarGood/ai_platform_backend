package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目专属智能体实体，对应 {@code project_agents} 表。
 *
 * <p>每个项目在创建时自动生成一个专属智能体。
 * 智能体聚合了项目关联的技能 System Prompt、知识库 RAG、工具调用能力，
 * 可回答「今天项目有什么 Bug」「帮我发布到生产」等项目相关问题。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
