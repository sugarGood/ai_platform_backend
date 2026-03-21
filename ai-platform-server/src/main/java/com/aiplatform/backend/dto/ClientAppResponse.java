package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.ClientApp;

import java.time.LocalDateTime;

/**
 * 研发客户端响应 DTO。
 *
 * @param id                    客户端 ID
 * @param code                  客户端编码
 * @param name                  客户端名称
 * @param icon                  图标
 * @param supportsMcp           是否支持 MCP
 * @param supportsCustomGateway 是否支持自定义网关
 * @param setupInstruction      接入指南
 * @param status                状态
 * @param createdAt             创建时间
 * @param updatedAt             更新时间
 */
public record ClientAppResponse(
        Long id,
        String code,
        String name,
        String icon,
        Boolean supportsMcp,
        Boolean supportsCustomGateway,
        String setupInstruction,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClientAppResponse from(ClientApp app) {
        return new ClientAppResponse(
                app.getId(),
                app.getCode(),
                app.getName(),
                app.getIcon(),
                app.getSupportsMcp(),
                app.getSupportsCustomGateway(),
                app.getSetupInstruction(),
                app.getStatus(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}
