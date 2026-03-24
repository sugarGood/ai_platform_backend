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
 * 技能定义实体，对应 skills 表。
 *
 * <p>技能是平台 AI 能力编排的核心单元，定义了角色 Prompt、关联知识库、绑定工具等配置。
 * 支持通过斜杠命令触发，并提供使用统计和满意度反馈追踪。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
