package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.dto.ChatCompletionMessage;
import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.entity.ProjectAgentRef;
import com.aiplatform.agent.gateway.mapper.ProjectAgentRefMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目专属智能体对话服务。
 *
 * <p>负责在标准网关链路之上，额外注入项目智能体的 System Prompt，
 * 并根据智能体配置的能力开关（RAG、技能、工具、部署、监控）控制上下文增强行为。</p>
 *
 * <p>处理流程：</p>
 * <ol>
 *   <li>根据 projectId 加载 {@link ProjectAgentRef} 配置</li>
 *   <li>将智能体 System Prompt 注入到消息列表最前面</li>
 *   <li>若配置了 preferredModel 则替换请求中的 model</li>
 *   <li>委托 {@link GatewayService} 走完整网关链路（含 RAG、技能注入、配额、用量记录）</li>
 * </ol>
 */
@Service
public class ProjectAgentChatService {

    private static final Logger log = LoggerFactory.getLogger(ProjectAgentChatService.class);

    /** 默认使用的模型（当项目智能体「乐知」未配置 preferredModel 时） */
    private static final String DEFAULT_MODEL = "gpt-4o";

    private final ProjectAgentRefMapper projectAgentRefMapper;
    private final GatewayService gatewayService;

    public ProjectAgentChatService(ProjectAgentRefMapper projectAgentRefMapper,
                                   GatewayService gatewayService) {
        this.projectAgentRefMapper = projectAgentRefMapper;
        this.gatewayService = gatewayService;
    }

    // ---------------------------------------------------------------
    // 同步对话
    // ---------------------------------------------------------------

    /**
     * 使用项目专属智能体进行同步对话。
     *
     * @param authorization Bearer token
     * @param projectId     项目 ID
     * @param request       原始聊天请求
     * @return 上游返回的原始 JSON 响应
     */
    public String chat(String authorization, Long projectId, ChatCompletionRequest request) {
        ProjectAgentRef agent = loadActiveAgent(projectId);
        ChatCompletionRequest enriched = injectAgentContext(agent, request);
        return gatewayService.chatCompletion(authorization, projectId, enriched);
    }

    // ---------------------------------------------------------------
    // 流式对话
    // ---------------------------------------------------------------

    /**
     * 使用项目专属智能体进行流式对话（SSE）。
     *
     * @param authorization Bearer token
     * @param projectId     项目 ID
     * @param request       原始聊天请求
     * @return SSE 流式响应
     */
    public Flux<String> chatStream(String authorization, Long projectId, ChatCompletionRequest request) {
        ProjectAgentRef agent = loadActiveAgent(projectId);
        ChatCompletionRequest enriched = injectAgentContext(agent, request);
        return gatewayService.chatCompletionStream(authorization, projectId, enriched);
    }

    // ---------------------------------------------------------------
    // 内部：加载智能体配置
    // ---------------------------------------------------------------

    private ProjectAgentRef loadActiveAgent(Long projectId) {
        ProjectAgentRef agent = projectAgentRefMapper.selectOne(
                Wrappers.<ProjectAgentRef>lambdaQuery()
                        .eq(ProjectAgentRef::getProjectId, projectId)
                        .eq(ProjectAgentRef::getStatus, "ACTIVE")
        );
        if (agent == null) {
            throw new AgentNotAvailableException(projectId);
        }
        return agent;
    }

    // ---------------------------------------------------------------
    // 内部：注入智能体上下文
    // ---------------------------------------------------------------

    /**
     * 将智能体 System Prompt 注入请求，并根据 preferredModel 替换模型。
     *
     * <p>注入逻辑：</p>
     * <ul>
     *   <li>若智能体有 {@code systemPrompt}，在消息列表最前面插入一条 role=system 消息</li>
     *   <li>若请求中已有 role=system 消息，则将智能体 prompt 前置拼接，避免覆盖调用方的 prompt</li>
     *   <li>若 {@code preferredModel} 不为空，替换请求中的 model 字段</li>
     * </ul>
     *
     * <p>注意：RAG 检索和技能注入由下游 {@link ProjectContextEnrichmentService} 根据
     * {@code enableRag} / {@code enableSkills} 开关控制，此处不重复处理。</p>
     */
    private ChatCompletionRequest injectAgentContext(ProjectAgentRef agent,
                                                     ChatCompletionRequest request) {
        List<ChatCompletionMessage> messages = new ArrayList<>(
                request.messages() != null ? request.messages() : List.of());

        // 注入 System Prompt
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            // 检查是否已存在 system 消息
            int existingSystemIdx = -1;
            for (int i = 0; i < messages.size(); i++) {
                if ("system".equals(messages.get(i).role())) {
                    existingSystemIdx = i;
                    break;
                }
            }

            if (existingSystemIdx >= 0) {
                // 将智能体 prompt 前置拼接到已有 system 消息
                ChatCompletionMessage existing = messages.get(existingSystemIdx);
                String merged = agent.getSystemPrompt() + "\n\n" + existing.content();
                messages.set(existingSystemIdx, new ChatCompletionMessage("system", merged));
            } else {
                // 插入新的 system 消息到最前面
                messages.add(0, new ChatCompletionMessage("system", agent.getSystemPrompt()));
            }
        }

        // 替换 preferredModel（若配置了的话）
        String model = (agent.getPreferredModel() != null && !agent.getPreferredModel().isBlank())
                ? agent.getPreferredModel()
                : (request.model() != null ? request.model() : DEFAULT_MODEL);

        return new ChatCompletionRequest(model, messages, request.temperature(), request.maxTokens());
    }

    // ---------------------------------------------------------------
    // 异常
    // ---------------------------------------------------------------

    public static class AgentNotAvailableException extends RuntimeException {
        public AgentNotAvailableException(Long projectId) {
            super("项目智能体不可用或未启用，projectId=" + projectId);
        }
    }
}
