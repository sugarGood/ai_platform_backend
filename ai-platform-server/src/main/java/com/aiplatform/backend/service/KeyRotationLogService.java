package com.aiplatform.backend.service;

import com.aiplatform.backend.entity.KeyRotationLog;
import com.aiplatform.backend.mapper.KeyRotationLogMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 密钥轮换日志业务服务。
 */
@Service
public class KeyRotationLogService {

    private final KeyRotationLogMapper keyRotationLogMapper;

    /** 构造函数。 */
    public KeyRotationLogService(KeyRotationLogMapper keyRotationLogMapper) {
        this.keyRotationLogMapper = keyRotationLogMapper;
    }

    /** 记录一条轮换日志。 */
    public KeyRotationLog create(KeyRotationLog log) {
        keyRotationLogMapper.insert(log);
        return log;
    }

    /** 按目标类型和 ID 查询轮换日志。 */
    public List<KeyRotationLog> listByTarget(String targetType, Long targetId) {
        return keyRotationLogMapper.selectList(
                Wrappers.<KeyRotationLog>lambdaQuery()
                        .eq(KeyRotationLog::getTargetType, targetType)
                        .eq(KeyRotationLog::getTargetId, targetId)
                        .orderByAsc(KeyRotationLog::getId)
        );
    }
}
