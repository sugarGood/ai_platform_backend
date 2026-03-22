package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.dto.ChatCompletionMessage;
import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.entity.KnowledgeBaseRef;
import com.aiplatform.agent.gateway.entity.ProjectAgentRef;
import com.aiplatform.agent.gateway.entity.ProjectKnowledgeConfigRef;
import com.aiplatform.agent.gateway.entity.ProjectSkillRef;
import com.aiplatform.agent.gateway.entity.SkillRef;
import com.aiplatform.agent.gateway.mapper.KnowledgeBaseRefMapper;
import com.aiplatform.agent.gateway.mapper.ProjectAgentRefMapper;
import com.aiplatform.agent.gateway.mapper.ProjectKnowledgeConfigRefMapper;
import com.aiplatform.agent.gateway.mapper.ProjectSkillRefMapper;
import com.aiplatform.agent.gateway.mapper.SkillRefMapper;
import com.aiplatform.agent.gateway.service.QdrantSearchService.ChunkResult;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目上下文增强服务。
 *
 * <p>增强行为受项目专属智能体（{@code project_agents}）的能力开关控制：</p>
 * <ul>
 *   <li>{@code enableSkills=false}：跳过技能 System Prompt 注入</li>
 *   <li>{@code enableRag=false}：跳过 RAG 知识库检索注入</li>
 *   <li>智能体不存在或已禁用：保持默认增强行为（全部启用）</li>
 * </ul>
 *
 * <p>增强内容追加在消息列表已有 system 消息之后（不覆盖），
 * 确保项目智能体 System Prompt（由 {@link ProjectAgentChatService} 注入）始终优先。</p>
 */
@Service
public class ProjectContextEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(ProjectContextEnrichmentService.class);

    private final ProjectSkillRefMapper projectSkillMapper;
    private final SkillRefMapper skillMapper;
    private final ProjectKnowledgeConfigRefMapper projectKnowledgeConfigMapper;
    private final KnowledgeBaseRefMapper knowledgeBaseMapper;
    private final ProjectAgentRefMapper projectAgentRefMapper;
    private final EmbeddingService embeddingService;
    private final QdrantSearchService qdrantSearchService;
    private final KnowledgeSearchLogService searchLogService;

    public ProjectContextEnrichmentService(
            ProjectSkillRefMapper projectSkillMapper,
            SkillRefMapper skillMapper,
            ProjectKnowledgeConfigRefMapper projectKnowledgeConfigMapper,
            KnowledgeBaseRefMapper knowledgeBaseMapper,
            ProjectAgentRefMapper projectAgentRefMapper,
            EmbeddingService embeddingService,
            QdrantSearchService qdrantSearchService,
            KnowledgeSearchLogService searchLogService) {
        this.projectSkillMapper = projectSkillMapper;
        this.skillMapper = skillMapper;
        this.projectKnowledgeConfigMapper = projectKnowledgeConfigMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.projectAgentRefMapper = projectAgentRefMapper;
        this.embeddingService = embeddingService;
        this.qdrantSearchService = qdrantSearchService;
        this.searchLogService = searchLogService;
    }

    /**
     * 对请求进行项目上下文增强。
     *
     * <p>若 projectId 为 null 则直接返回原始请求。否则按智能体开关决定
     * 是否追加技能 Prompt 和 RAG 知识片段。</p>
     */
    public ChatCompletionRequest enrich(Long projectId, Long userId, ChatCompletionRequest request) {
        if (projectId == null) {
            return request;
        }

        // 加载项目智能体配置，读取能力开关
        ProjectAgentRef agentConfig = loadAgentConfig(projectId);
        boolean skillsEnabled = agentConfig == null || !Boolean.FALSE.equals(agentConfig.getEnableSkills());
        boolean ragEnabled    = agentConfig == null || !Boolean.FALSE.equals(agentConfig.getEnableRag());

        String appendedContext = buildAppendedContext(projectId, userId, request, skillsEnabled, ragEnabled);
        if (appendedContext.isEmpty()) {
            return request;
        }

        // 将追加内容合并到已有 system 消息末尾（保留智能体 prompt 在前）
        List<ChatCompletionMessage> enrichedMessages = new ArrayList<>(
                request.messages() != null ? request.messages() : List.of());

        int systemIdx = -1;
        for (int i = 0; i < enrichedMessages.size(); i++) {
            if ("system".equals(enrichedMessages.get(i).role())) {
                systemIdx = i;
                break;
            }
        }

        if (systemIdx >= 0) {
            ChatCompletionMessage existing = enrichedMessages.get(systemIdx);
            enrichedMessages.set(systemIdx,
                    new ChatCompletionMessage("system", existing.content() + "\n\n" + appendedContext));
        } else {
            enrichedMessages.add(0, new ChatCompletionMessage("system", appendedContext));
        }

        return new ChatCompletionRequest(
                request.model(), enrichedMessages, request.temperature(), request.maxTokens());
    }

    private String buildAppendedContext(Long projectId, Long userId, ChatCompletionRequest request,
                                        boolean skillsEnabled, boolean ragEnabled) {
        StringBuilder sb = new StringBuilder();
        if (skillsEnabled) {
            appendSkillPrompts(projectId, sb);
        } else {
            log.debug("enableSkills=false，跳过技能注入，projectId={}", projectId);
        }
        if (ragEnabled) {
            appendRagChunks(projectId, userId, request, sb);
        } else {
            log.debug("enableRag=false，跳过 RAG 检索，projectId={}", projectId);
        }
        return sb.toString();
    }

    private ProjectAgentRef loadAgentConfig(Long projectId) {
        try {
            return projectAgentRefMapper.selectOne(
                    Wrappers.<ProjectAgentRef>lambdaQuery()
                            .eq(ProjectAgentRef::getProjectId, projectId)
                            .eq(ProjectAgentRef::getStatus, "ACTIVE")
            );
        } catch (Exception e) {
            log.warn("加载项目智能体配置失败，将使用默认增强行为，projectId={}: {}", projectId, e.getMessage());
            return null;
        }
    }

    private void appendSkillPrompts(Long projectId, StringBuilder sb) {
        List<ProjectSkillRef> projectSkills = projectSkillMapper.selectList(
                Wrappers.<ProjectSkillRef>lambdaQuery()
                        .eq(ProjectSkillRef::getProjectId, projectId)
                        .eq(ProjectSkillRef::getStatus, "ACTIVE")
        );
        if (projectSkills.isEmpty()) return;

        List<Long> skillIds = projectSkills.stream()
                .map(ProjectSkillRef::getSkillId).collect(Collectors.toList());

        List<SkillRef> skills = skillMapper.selectList(
                Wrappers.<SkillRef>lambdaQuery()
                        .in(SkillRef::getId, skillIds)
                        .eq(SkillRef::getStatus, "PUBLISHED")
        );

        for (SkillRef skill : skills) {
            if (skill.getSystemPrompt() != null && !skill.getSystemPrompt().isBlank()) {
                if (!sb.isEmpty()) sb.append("\n\n");
                sb.append("## ").append(skill.getName()).append("\n");
                sb.append(skill.getSystemPrompt());
            }
        }
    }

    private void appendRagChunks(Long projectId, Long userId,
                                  ChatCompletionRequest request, StringBuilder sb) {
        List<ProjectKnowledgeConfigRef> kbConfigs = projectKnowledgeConfigMapper.selectList(
                Wrappers.<ProjectKnowledgeConfigRef>lambdaQuery()
                        .eq(ProjectKnowledgeConfigRef::getProjectId, projectId)
                        .eq(ProjectKnowledgeConfigRef::getStatus, "ACTIVE")
        );
        if (kbConfigs.isEmpty()) return;

        List<Long> kbIds = kbConfigs.stream()
                .map(ProjectKnowledgeConfigRef::getKbId).collect(Collectors.toList());

        List<KnowledgeBaseRef> autoInjectKbs = knowledgeBaseMapper.selectList(
                Wrappers.<KnowledgeBaseRef>lambdaQuery()
                        .in(KnowledgeBaseRef::getId, kbIds)
                        .eq(KnowledgeBaseRef::getInjectMode, "AUTO_INJECT")
                        .eq(KnowledgeBaseRef::getStatus, "ACTIVE")
        );
        if (autoInjectKbs.isEmpty()) return;

        String queryText = extractLastUserMessage(request);
        if (queryText == null || queryText.isBlank()) {
            log.debug("未找到用户消息，跳过 RAG 检索");
            return;
        }

        float[] queryVector;
        try {
            queryVector = embeddingService.embed(queryText);
        } catch (EmbeddingService.EmbeddingException e) {
            log.warn("Embedding 服务不可用，跳过 RAG 注入: {}", e.getMessage());
            return;
        }

        List<ChunkResult> allChunks = new ArrayList<>();
        for (KnowledgeBaseRef kb : autoInjectKbs) {
            long searchStart = System.currentTimeMillis();
            List<ChunkResult> chunks = qdrantSearchService.search(kb.getId(), queryVector);
            long latencyMs = System.currentTimeMillis() - searchStart;
            searchLogService.logAsync(kb.getId(), projectId, userId, queryText, chunks, latencyMs);
            allChunks.addAll(chunks);
            log.debug("RAG 检索，kb_id={}，命中 {} 个 chunk，耗时 {}ms",
                    kb.getId(), chunks.size(), latencyMs);
        }

        if (allChunks.isEmpty()) return;

        allChunks.sort((a, b) -> Double.compare(b.score(), a.score()));

        if (!sb.isEmpty()) sb.append("\n\n");
        sb.append("## 参考知识\n");
        sb.append("以下是与当前问题相关的知识库内容，请优先参考：\n\n");
        for (ChunkResult chunk : allChunks) {
            if (chunk.chunkText() == null || chunk.chunkText().isBlank()) continue;
            sb.append("【");
            if (chunk.docTitle() != null) sb.append(chunk.docTitle());
            sb.append("】\n");
            sb.append(chunk.chunkText()).append("\n\n");
        }
    }

    private String extractLastUserMessage(ChatCompletionRequest request) {
        if (request.messages() == null || request.messages().isEmpty()) return null;
        List<ChatCompletionMessage> messages = request.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatCompletionMessage msg = messages.get(i);
            if ("user".equals(msg.role()) && msg.content() != null) {
                return msg.content();
            }
        }
        return null;
    }
}
