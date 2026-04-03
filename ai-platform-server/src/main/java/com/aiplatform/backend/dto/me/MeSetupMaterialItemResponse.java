package com.aiplatform.backend.dto.me;

import java.time.LocalDateTime;

/**
 * 当前用户客户端接入材料。
 *
 * @param clientAppId            客户端 ID
 * @param code                   客户端编码
 * @param name                   客户端名称
 * @param icon                   客户端图标
 * @param supportsMcp            是否支持 MCP
 * @param supportsCustomGateway  是否支持自定义网关
 * @param setupInstruction       接入说明
 * @param bindingStatus          当前用户绑定状态
 * @param lastActiveAt           当前用户最近活跃时间
 */
public record MeSetupMaterialItemResponse(
        Long clientAppId,
        String code,
        String name,
        String icon,
        Boolean supportsMcp,
        Boolean supportsCustomGateway,
        String setupInstruction,
        String bindingStatus,
        LocalDateTime lastActiveAt
) {
}
