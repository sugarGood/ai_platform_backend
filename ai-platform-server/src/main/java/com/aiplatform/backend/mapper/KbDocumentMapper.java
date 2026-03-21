package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.KbDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供知识库文档表的基础 CRUD 操作。</p>
 */
@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {
}
