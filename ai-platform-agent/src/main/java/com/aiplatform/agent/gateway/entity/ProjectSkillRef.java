package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目技能关联引用实体（网关只读视图）。
 *
 * <p>映射 {@code project_skills} 表，网关通过该实体查询项目启用了哪些技能。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_skills")
public class ProjectSkillRef {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long skillId;
    private String status;
}
