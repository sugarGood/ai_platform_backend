package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.KnowledgeSearchLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库检索日志 Mapper。
 *
 * <p>管理端仅做 COUNT 聚合；写入在 ai-platform-agent 网关侧完成。</p>
 */
@Mapper
public interface KnowledgeSearchLogMapper extends BaseMapper<KnowledgeSearchLog> {
}
