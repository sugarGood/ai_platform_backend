package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建研发客户端的请求参数。
 *
 * @param code                  客户端编码（必填，如 claude_code）
 * @param name                  客户端名称（必填）
 * @param icon                  图标
 * @param supportsMcp           是否支持 MCP 协议
 * @param supportsCustomGateway 是否支持自定义网关
 * @param setupInstruction      接入指南（Markdown）
 */
public record CreateClientAppRequest(
        @NotBlank(message = "Client app code must not be blank")
        String code,
        @NotBlank(message = "Client app name must not be blank")
        String name,
        String icon,
        Boolean supportsMcp,
        Boolean supportsCustomGateway,
        String setupInstruction
) {
}
