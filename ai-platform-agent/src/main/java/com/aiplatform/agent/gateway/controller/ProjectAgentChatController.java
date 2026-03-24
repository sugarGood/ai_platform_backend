package com.aiplatform.agent.gateway.controller;

import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.service.ProjectAgentChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 项目专属智能体对话控制器。
 *
 * <p>为项目空间提供 AI 对话入口，智能体具备项目上下文感知能力：
 * 自动注入项目 System Prompt、RAG 知识库检索、技能增强，
 * 可回答「今天项目有什么 Bug？」「帮我查一下生产环境的告警」等项目相关问题。</p>
 *
 * <p>API 基路径：{@code /api/agent/projects/{projectId}/chat}</p>
 */
@RestController
@RequestMapping("/api/agent/projects/{projectId}/chat")
public class ProjectAgentChatController {

    private final ProjectAgentChatService projectAgentChatService;

    public ProjectAgentChatController(ProjectAgentChatService projectAgentChatService) {
        this.projectAgentChatService = projectAgentChatService;
    }

    /**
     * 项目智能体对话（同步模式）。
     *
     * <p>示例问题：</p>
     * <ul>
     *   <li>今天项目运行有什么 Bug 吗？</li>
     *   <li>项目最近有什么问题？</li>
     *   <li>帮我查一下当前告警</li>
     *   <li>帮我发布到生产环境</li>
     * </ul>
     *
     * @param authorization Bearer token（平台凭证）
     * @param projectId     项目 ID（路径参数）
     * @param request       聊天请求
     * @return 上游返回的原始 JSON 响应
     */
    @PostMapping("/completions")
    public ResponseEntity<String> chat(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long projectId,
            @RequestBody ChatCompletionRequest request) {
        String response = projectAgentChatService.chat(authorization, projectId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 项目智能体对话（SSE 流式模式）。
     *
     * @param authorization Bearer token（平台凭证）
     * @param projectId     项目 ID（路径参数）
     * @param request       聊天请求
     * @return SSE 流式响应
     */
    @PostMapping(value = "/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long projectId,
            @RequestBody ChatCompletionRequest request) {
        return projectAgentChatService.chatStream(authorization, projectId, request)
                .map(data -> ServerSentEvent.<String>builder().data(data).build());
    }

}
