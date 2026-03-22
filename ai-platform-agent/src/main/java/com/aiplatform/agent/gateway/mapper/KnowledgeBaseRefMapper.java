package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.KnowledgeBaseRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库引用数据访问接口。
 *
 * <p>网关通过该 Mapper 读取知识库的注入模式等配置。</p>
 */
@Mapper
public interface KnowledgeBaseRefMapper extends BaseMapper<KnowledgeBaseRef> {
}
