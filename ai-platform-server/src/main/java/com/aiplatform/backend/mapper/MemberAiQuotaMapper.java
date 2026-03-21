package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.MemberAiQuota;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成员 AI 配额数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供成员配额表的基础 CRUD 操作。</p>
 */
@Mapper
public interface MemberAiQuotaMapper extends BaseMapper<MemberAiQuota> {
}
