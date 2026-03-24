package com.aiplatform.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 调用内网 AI Agent（向量化入库等）的 {@link WebClient}。
 */
@Configuration
public class AgentWebClientConfig {

    @Bean(name = "agentWebClient")
    public WebClient agentWebClient(WebClient.Builder builder,
                                    @Value("${ai.agent.base-url}") String agentBaseUrl) {
        return builder.baseUrl(agentBaseUrl).build();
    }
}
