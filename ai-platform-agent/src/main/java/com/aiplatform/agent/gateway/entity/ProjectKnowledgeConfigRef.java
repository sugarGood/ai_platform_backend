package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 项目知识库配置引用实体（网关只读视图）。
 *
 * <p>映射 {@code project_knowledge_configs} 表，网关通过该实体查询项目关联了哪些知识库
 * 及其检索权重配置。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_knowledge_configs")
public class ProjectKnowledgeConfigRef {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long kbId;
    private BigDecimal searchWeight;
    private String status;
}
