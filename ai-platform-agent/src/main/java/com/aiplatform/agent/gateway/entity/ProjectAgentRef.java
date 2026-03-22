package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 项目专属智能体引用实体（网关只读视图）。
 *
 * <p>映射 {@code project_agents} 表，网关通过该实体读取项目智能体的配置，
 * 包括 System Prompt、能力开关，用于在请求处理链路中优先注入项目智能体上下文。</p>
 */
@Data
@TableName("project_agents")
public class ProjectAgentRef {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属项目 ID */
    private Long projectId;

    /** 智能体名称 */
    private String name;

    /** 项目智能体全局 System Prompt */
    private String systemPrompt;

    /** 优先使用的模型代码，null 时使用平台默认 */
    private String preferredModel;

    /** 是否启用知识库 RAG 增强 */
    private Boolean enableRag;

    /** 是否启用项目技能注入 */
    private Boolean enableSkills;

    /** 是否启用工具调用 */
    private Boolean enableTools;

    /** 是否启用部署指令能力 */
    private Boolean enableDeploy;

    /** 是否启用监控告警查询能力 */
    private Boolean enableMonitoring;

    /** 状态：ACTIVE / DISABLED */
    private String status;
}
