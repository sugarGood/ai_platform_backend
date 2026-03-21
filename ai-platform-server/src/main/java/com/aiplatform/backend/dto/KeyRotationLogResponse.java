package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.KeyRotationLog;

import java.time.LocalDateTime;

/**
 * 密钥轮换日志响应 DTO。
 *
 * @param id           日志 ID
 * @param targetType   轮换目标类型
 * @param targetId     目标 ID
 * @param targetLabel  目标标识
 * @param rotationType 轮换方式
 * @param oldKeyPrefix 旧密钥前缀
 * @param newKeyPrefix 新密钥前缀
 * @param result       轮换结果
 * @param errorMessage 失败原因
 * @param operatedBy   操作人用户 ID
 * @param createdAt    创建时间
 */
public record KeyRotationLogResponse(
        Long id,
        String targetType,
        Long targetId,
        String targetLabel,
        String rotationType,
        String oldKeyPrefix,
        String newKeyPrefix,
        String result,
        String errorMessage,
        Long operatedBy,
        LocalDateTime createdAt
) {
    public static KeyRotationLogResponse from(KeyRotationLog log) {
        return new KeyRotationLogResponse(
                log.getId(),
                log.getTargetType(),
                log.getTargetId(),
                log.getTargetLabel(),
                log.getRotationType(),
                log.getOldKeyPrefix(),
                log.getNewKeyPrefix(),
                log.getResult(),
                log.getErrorMessage(),
                log.getOperatedBy(),
                log.getCreatedAt()
        );
    }
}
