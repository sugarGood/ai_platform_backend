package com.aiplatform.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档全局配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 中台 API")
                        .description("Enterprise AI Platform 后端接口文档（基于 JavaDoc 注释自动生成）")
                        .version("1.0.0"));
    }
}
