package com.aiplatform.agent.gateway.dto;

import java.util.List;

/**
 * 聊天补全请求参数。
 *
 * <p>封装客户端发送给网关的聊天补全请求数据，网关验证后将其转发至上游 AI 供应商。</p>
 *
 * @param model       模型标识编码（如 gpt-4o、claude-3）
 * @param messages    消息列表，包含对话上下文
 * @param temperature 温度参数，控制生成文本的随机性（0.0~2.0）
 * @param maxTokens   最大生成 Token 数
 */
public record ChatCompletionRequest(
        String model,
        List<ChatCompletionMessage> messages,
        Double temperature,
        Integer maxTokens
) {
}
