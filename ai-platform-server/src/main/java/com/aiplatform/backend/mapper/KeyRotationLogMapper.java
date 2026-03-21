package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.KeyRotationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 密钥轮换日志数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供 {@code key_rotation_logs} 表的基础 CRUD 操作。</p>
 */
@Mapper
public interface KeyRotationLogMapper extends BaseMapper<KeyRotationLog> {
}
