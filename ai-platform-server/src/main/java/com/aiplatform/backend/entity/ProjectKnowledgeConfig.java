package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目知识库继承配置实体，对应 project_knowledge_configs 表。
 *
 * <p>用于管理项目对全局知识库的引用关系，可为每个引用配置独立的检索权重。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_knowledge_configs")
public class ProjectKnowledgeConfig {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目ID */
    private Long projectId;

    /** 全局知识库ID */
    private Long kbId;

    /** 检索权重，取值范围 0~1 */
    private BigDecimal searchWeight;

    /** 状态：ACTIVE / INACTIVE */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

}
