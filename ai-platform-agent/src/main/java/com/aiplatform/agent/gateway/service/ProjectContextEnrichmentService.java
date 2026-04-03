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
import java.util.Comparator;
import java.util.List;

/**
 * 项目上下文增强服务，按项目智能体开关注入技能 Prompt 和 RAG 片段。
 */
@Service
public class ProjectContextEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(ProjectContextEnrichmentService.class);
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String INJECT_MODE_AUTO = "AUTO_INJECT";

    private final ProjectSkillRefMapper projectSkillMapper;
    private final SkillRefMapper skillMapper;
    private final ProjectKnowledgeConfigRefMapper projectKnowledgeConfigMapper;
    private final KnowledgeBaseRefMapper knowledgeBaseMapper;
    private final ProjectAgentRefMapper projectAgentRefMapper;
    private final EmbeddingService embeddingService;
    private final QdrantSearchService qdrantSearchService;
    private final KnowledgeSearchLogService searchLogService;

    public ProjectContextEnrichmentService(ProjectSkillRefMapper projectSkillMapper,
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
     */
    public ChatCompletionRequest enrich(Long projectId, Long userId, ChatCompletionRequest request) {
        if (projectId == null) {
            return request;
        }

        ProjectAgentRef agentConfig = loadAgentConfig(projectId);
        String appendedContext = buildAppendedContext(projectId, userId, request, agentConfig);
        if (appendedContext.isEmpty()) {
            return request;
        }

        return new ChatCompletionRequest(
                request.model(),
                mergeSystemMessages(request.messages(), appendedContext),
                request.temperature(),
                request.maxTokens());
    }

    private String buildAppendedContext(Long projectId,
                                        Long userId,
                                        ChatCompletionRequest request,
                                        ProjectAgentRef agentConfig) {
        boolean skillsEnabled = agentConfig == null || !Boolean.FALSE.equals(agentConfig.getEnableSkills());
        boolean ragEnabled = agentConfig == null || !Boolean.FALSE.equals(agentConfig.getEnableRag());

        StringBuilder context = new StringBuilder();
        if (skillsEnabled) {
            appendSkillPrompts(projectId, context);
        } else {
            log.debug("enableSkills=false，跳过技能注入，projectId={}", projectId);
        }
        if (ragEnabled) {
            appendRagChunks(projectId, userId, request, context);
        } else {
            log.debug("enableRag=false，跳过 RAG 检索，projectId={}", projectId);
        }
        return context.toString();
    }

    private List<ChatCompletionMessage> mergeSystemMessages(List<ChatCompletionMessage> originalMessages,
                                                            String appendedContext) {
        List<ChatCompletionMessage> messages = new ArrayList<>(originalMessages != null ? originalMessages : List.of());
        for (int i = 0; i < messages.size(); i++) {
            ChatCompletionMessage message = messages.get(i);
            if ("system".equals(message.role())) {
                messages.set(i, new ChatCompletionMessage(
                        "system",
                        message.content() + "\n\n" + appendedContext));
                return messages;
            }
        }
        messages.add(0, new ChatCompletionMessage("system", appendedContext));
        return messages;
    }

    private ProjectAgentRef loadAgentConfig(Long projectId) {
        try {
            return projectAgentRefMapper.selectOne(
                    Wrappers.<ProjectAgentRef>lambdaQuery()
                            .eq(ProjectAgentRef::getProjectId, projectId)
                            .eq(ProjectAgentRef::getStatus, STATUS_ACTIVE)
            );
        } catch (Exception e) {
            log.warn("加载项目智能体配置失败，将使用默认增强行为，projectId={}: {}", projectId, e.getMessage());
            return null;
        }
    }

    private void appendSkillPrompts(Long projectId, StringBuilder context) {
        List<ProjectSkillRef> projectSkills = projectSkillMapper.selectList(
                Wrappers.<ProjectSkillRef>lambdaQuery()
                        .eq(ProjectSkillRef::getProjectId, projectId)
                        .eq(ProjectSkillRef::getStatus, STATUS_ACTIVE)
        );
        if (projectSkills.isEmpty()) {
            return;
        }

        List<Long> skillIds = projectSkills.stream().map(ProjectSkillRef::getSkillId).toList();
        List<SkillRef> skills = skillMapper.selectList(
                Wrappers.<SkillRef>lambdaQuery()
                        .in(SkillRef::getId, skillIds)
                        .eq(SkillRef::getStatus, STATUS_PUBLISHED)
        );

        for (SkillRef skill : skills) {
            if (skill.getSystemPrompt() == null || skill.getSystemPrompt().isBlank()) {
                continue;
            }
            if (!context.isEmpty()) {
                context.append("\n\n");
            }
            context.append("## ").append(skill.getName()).append("\n");
            context.append(skill.getSystemPrompt());
        }
    }

    private void appendRagChunks(Long projectId, Long userId, ChatCompletionRequest request, StringBuilder context) {
        List<KnowledgeBaseRef> autoInjectKnowledgeBases = loadAutoInjectKnowledgeBases(projectId);
        if (autoInjectKnowledgeBases.isEmpty()) {
            return;
        }

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

        List<ChunkResult> allChunks = searchKnowledgeBases(autoInjectKnowledgeBases, projectId, userId, queryText, queryVector);
        if (allChunks.isEmpty()) {
            return;
        }

        allChunks.sort(Comparator.comparingDouble(ChunkResult::score).reversed());
        if (!context.isEmpty()) {
            context.append("\n\n");
        }
        context.append("## 参考知识\n");
        context.append("以下是与当前问题相关的知识库内容，请优先参考：\n\n");
        for (ChunkResult chunk : allChunks) {
            if (chunk.chunkText() == null || chunk.chunkText().isBlank()) {
                continue;
            }
            context.append("《");
            if (chunk.docTitle() != null) {
                context.append(chunk.docTitle());
            }
            context.append("》\n");
            context.append(chunk.chunkText()).append("\n\n");
        }
    }

    private List<KnowledgeBaseRef> loadAutoInjectKnowledgeBases(Long projectId) {
        List<ProjectKnowledgeConfigRef> knowledgeConfigs = projectKnowledgeConfigMapper.selectList(
                Wrappers.<ProjectKnowledgeConfigRef>lambdaQuery()
                        .eq(ProjectKnowledgeConfigRef::getProjectId, projectId)
                        .eq(ProjectKnowledgeConfigRef::getStatus, STATUS_ACTIVE)
        );
        if (knowledgeConfigs.isEmpty()) {
            return List.of();
        }

        List<Long> kbIds = knowledgeConfigs.stream()
                .filter(this::shouldAutoInject)
                .map(ProjectKnowledgeConfigRef::getKbId)
                .toList();
        if (kbIds.isEmpty()) {
            return List.of();
        }

        return knowledgeBaseMapper.selectList(
                Wrappers.<KnowledgeBaseRef>lambdaQuery()
                        .in(KnowledgeBaseRef::getId, kbIds)
                        .eq(KnowledgeBaseRef::getInjectMode, INJECT_MODE_AUTO)
                        .eq(KnowledgeBaseRef::getStatus, STATUS_ACTIVE)
        );
    }

    private boolean shouldAutoInject(ProjectKnowledgeConfigRef knowledgeConfig) {
        String injectMode = knowledgeConfig.getInjectMode();
        return injectMode == null || INJECT_MODE_AUTO.equals(injectMode);
    }

    private List<ChunkResult> searchKnowledgeBases(List<KnowledgeBaseRef> knowledgeBases,
                                                   Long projectId,
                                                   Long userId,
                                                   String queryText,
                                                   float[] queryVector) {
        List<ChunkResult> allChunks = new ArrayList<>();
        for (KnowledgeBaseRef knowledgeBase : knowledgeBases) {
            long searchStart = System.currentTimeMillis();
            List<ChunkResult> chunks = qdrantSearchService.search(knowledgeBase.getId(), queryVector);
            long latencyMs = System.currentTimeMillis() - searchStart;
            searchLogService.logAsync(knowledgeBase.getId(), projectId, userId, queryText, chunks, latencyMs);
            allChunks.addAll(chunks);
            log.debug("RAG 检索，kbId={}，命中 {} 个 chunk，耗时 {}ms",
                    knowledgeBase.getId(), chunks.size(), latencyMs);
        }
        return allChunks;
    }

    private String extractLastUserMessage(ChatCompletionRequest request) {
        if (request.messages() == null || request.messages().isEmpty()) {
            return null;
        }
        List<ChatCompletionMessage> messages = request.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatCompletionMessage message = messages.get(i);
            if ("user".equals(message.role()) && message.content() != null) {
                return message.content();
            }
        }
        return null;
    }
}
