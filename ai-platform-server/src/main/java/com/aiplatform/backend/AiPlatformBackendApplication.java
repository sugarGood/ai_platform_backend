package com.aiplatform.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 平台后端服务启动入口。
 */
@SpringBootApplication
@MapperScan("com.aiplatform.backend")
public class AiPlatformBackendApplication {

    
    public static void main(String[] args) {
        SpringApplication.run(AiPlatformBackendApplication.class, args);
    }
}
