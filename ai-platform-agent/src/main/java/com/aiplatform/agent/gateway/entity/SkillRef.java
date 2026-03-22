package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能引用实体（网关只读视图）。
 *
 * <p>映射 {@code skills} 表，网关模块通过该实体读取项目关联技能的
 * System Prompt 等配置，用于上下文增强。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skills")
public class SkillRef {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String skillKey;
    private String name;
    private String systemPrompt;
    private String knowledgeRefs;
    private String boundTools;
    private String scope;
    private Long projectId;
    private String status;
}
