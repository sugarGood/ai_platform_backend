package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建技能请求 DTO。
 *
 * @param skillKey      技能唯一标识（必填）
 * @param name          技能名称（必填）
 * @param description   技能描述
 * @param scope         作用域：GLOBAL / PROJECT（必填）
 * @param projectId     所属项目ID，scope 为 PROJECT 时需指定
 * @param category      技能分类：ENGINEERING / QUALITY / SECURITY 等
 * @param systemPrompt  角色 Prompt 模板
 * @param knowledgeRefs 关联知识库引用（JSON）
 * @param boundTools    绑定工具列表（JSON）
 * @param parameters    用户可配参数（JSON）
 * @param slashCommand  斜杠触发命令
 * @param version       版本号，默认 1.0.0
 */
public record CreateSkillRequest(
        @NotBlank String skillKey,
        @NotBlank String name,
        String description,
        @NotBlank String scope,
        Long projectId,
        String category,
        String systemPrompt,
        String knowledgeRefs,
        String boundTools,
        String parameters,
        String slashCommand,
        String version
) {
}
