package com.aiplatform.agent.gateway.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Embedding 服务。
 *
 * <p>调用本地 Ollama 的 bge-m3 模型，将文本转换为向量（float 数组）。
 * 向量用于在 Qdrant 中进行语义相似度检索。</p>
 *
 * <p>接口格式兼容 OpenAI /v1/embeddings 规范：
 * POST {ollama.base-url}/v1/embeddings
 * Body: {"model": "bge-m3", "input": "文本"}
 * 返回: {"data": [{"embedding": [0.023, -0.187, ...]}]}</p>
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final WebClient webClient;
    private final String embeddingModel;

    public EmbeddingService(
            WebClient.Builder webClientBuilder,
            @Value("${ai.rag.ollama.base-url}") String ollamaBaseUrl,
            @Value("${ai.rag.ollama.embedding-model}") String embeddingModel) {
        this.webClient = webClientBuilder.baseUrl(ollamaBaseUrl).build();
        this.embeddingModel = embeddingModel;
    }

    /**
     * 将文本转换为向量。
     *
     * <p>调用 Ollama bge-m3 模型生成 1024 维 float 向量。
     * 此向量可直接用于 Qdrant 向量相似度检索。</p>
     *
     * @param text 待向量化的文本（用户问题或文档片段）
     * @return float 向量数组，长度 1024
     * @throws EmbeddingException 调用 Ollama 失败时抛出
     */
    public float[] embed(String text) {
        log.debug("生成 embedding，model={}, textLen={}", embeddingModel, text.length());

        EmbeddingRequest requestBody = new EmbeddingRequest(embeddingModel, text);

        try {
            EmbeddingResponse response = webClient.post()
                    .uri("/v1/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block();

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new EmbeddingException("Ollama 返回空 embedding 结果");
            }

            List<Double> vector = response.data().get(0).embedding();
            float[] result = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                result[i] = vector.get(i).floatValue();
            }

            log.debug("embedding 生成成功，维度={}", result.length);
            return result;

        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("调用 Ollama embedding 接口失败: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // 请求 / 响应 DTO（Ollama OpenAI 兼容格式）
    // ---------------------------------------------------------------

    private record EmbeddingRequest(String model, String input) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingResponse(List<EmbeddingData> data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingData(
            @JsonProperty("embedding") List<Double> embedding
    ) {}

    /**
     * Embedding 调用异常。
     */
    public static class EmbeddingException extends RuntimeException {
        public EmbeddingException(String message) { super(message); }
        public EmbeddingException(String message, Throwable cause) { super(message, cause); }
    }
}
