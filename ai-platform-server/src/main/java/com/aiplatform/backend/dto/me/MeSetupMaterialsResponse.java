package com.aiplatform.backend.dto.me;

import java.util.List;

/**
 * 当前用户接入材料响应。
 *
 * @param materials 按客户端聚合的接入材料
 */
public record MeSetupMaterialsResponse(
        List<MeSetupMaterialItemResponse> materials
) {
}
