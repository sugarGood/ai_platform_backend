package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.KnowledgeSearchLogRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库检索日志 Mapper。
 *
 * <p>网关在每次 RAG 检索后异步写入检索日志，用于命中率统计和质量分析。</p>
 */
@Mapper
public interface KnowledgeSearchLogRefMapper extends BaseMapper<KnowledgeSearchLogRef> {
}
