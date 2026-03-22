package com.aiplatform.backend.dto;

/**
 * 更新项目专属智能体配置的请求 DTO。
 *
 * <p>所有字段均为可选，仅传入需要修改的字段。</p>
 */
public record UpdateProjectAgentRequest(
        /** 智能体名称 */
        String name,
        /** 智能体描述 */
        String description,
        /** 头像图标 */
        String avatarIcon,
        /** 全局 System Prompt（覆盖默认生成的内容） */
        String systemPrompt,
        /** 优先使用的模型代码，null 表示使用平台默认 */
        String preferredModel,
        /** 是否启用知识库 RAG 增强 */
        Boolean enableRag,
        /** 是否启用项目技能注入 */
        Boolean enableSkills,
        /** 是否启用工具调用 */
        Boolean enableTools,
        /** 是否启用部署指令能力 */
        Boolean enableDeploy,
        /** 是否启用监控告警查询能力 */
        Boolean enableMonitoring,
        /** 状态：ACTIVE / DISABLED */
        String status
) {
}
