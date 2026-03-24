package com.aiplatform.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置：知识库向量化等耗时操作在独立线程池执行，避免阻塞 HTTP 线程。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "knowledgeIngestExecutor")
    public Executor knowledgeIngestExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("kb-ingest-");
        executor.initialize();
        return executor;
    }
}
