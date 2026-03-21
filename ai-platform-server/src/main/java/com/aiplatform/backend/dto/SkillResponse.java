package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.Skill;

import java.time.LocalDateTime;

/**
 * 技能响应 DTO。
 *
 * <p>用于向客户端返回技能的完整信息，包括配置详情和使用统计。</p>
 *
 * @param id               技能ID
 * @param skillKey         技能唯一标识
 * @param name             技能名称
 * @param description      技能描述
 * @param scope            作用域
 * @param projectId        所属项目ID
 * @param category         技能分类
 * @param systemPrompt     角色 Prompt 模板
 * @param knowledgeRefs    关联知识库引用
 * @param boundTools       绑定工具列表
 * @param parameters       用户可配参数
 * @param slashCommand     斜杠触发命令
 * @param version          版本号
 * @param status           发布状态
 * @param usageCount       累计使用次数
 * @param satisfactionUp   正反馈数
 * @param satisfactionDown 负反馈数
 * @param createdBy        创建者ID
 * @param createdAt        创建时间
 * @param publishedAt      发布时间
 */
public record SkillResponse(
        Long id,
        String skillKey,
        String name,
        String description,
        String scope,
        Long projectId,
        String category,
        String systemPrompt,
        String knowledgeRefs,
        String boundTools,
        String parameters,
        String slashCommand,
        String version,
        String status,
        Long usageCount,
        Integer satisfactionUp,
        Integer satisfactionDown,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime publishedAt
) {
    /**
     * 将技能实体转换为响应 DTO。
     *
     * @param s 技能实体
     * @return 技能响应 DTO
     */
    public static SkillResponse from(Skill s) {
        return new SkillResponse(
                s.getId(), s.getSkillKey(), s.getName(), s.getDescription(),
                s.getScope(), s.getProjectId(), s.getCategory(),
                s.getSystemPrompt(), s.getKnowledgeRefs(), s.getBoundTools(),
                s.getParameters(), s.getSlashCommand(), s.getVersion(),
                s.getStatus(), s.getUsageCount(), s.getSatisfactionUp(),
                s.getSatisfactionDown(), s.getCreatedBy(), s.getCreatedAt(),
                s.getPublishedAt()
        );
    }
}
