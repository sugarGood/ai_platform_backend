package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.ProviderFailoverPolicy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 故障转移策略数据访问接口。
 * <p>继承 MyBatis Plus 的 {@link BaseMapper}，提供故障转移策略表的基础 CRUD 操作。</p>
 */
@Mapper
public interface ProviderFailoverPolicyMapper extends BaseMapper<ProviderFailoverPolicy> {
}
