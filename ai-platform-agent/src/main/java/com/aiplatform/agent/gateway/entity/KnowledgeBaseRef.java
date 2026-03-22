package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库引用实体（网关只读视图）。
 *
 * <p>映射 {@code knowledge_bases} 表，网关通过该实体获取知识库的注入模式等配置，
 * 用于判断是否在上下文增强时自动注入知识库信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_bases")
public class KnowledgeBaseRef {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String scope;
    private Long projectId;
    private String injectMode;
    private String status;
}
