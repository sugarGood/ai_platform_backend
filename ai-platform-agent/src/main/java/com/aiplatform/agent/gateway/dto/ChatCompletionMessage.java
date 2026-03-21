package com.aiplatform.agent.gateway.dto;

/**
 * 聊天消息。
 *
 * <p>表示聊天补全请求中的单条消息，包含角色和内容信息。</p>
 *
 * @param role    消息角色（system: 系统提示 / user: 用户输入 / assistant: 助手回复）
 * @param content 消息内容
 */
public record ChatCompletionMessage(
        String role,
        String content
) {
}
