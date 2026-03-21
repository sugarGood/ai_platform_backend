package com.aiplatform.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 网关模块启动入口。
 *
 * <p>该模块作为 AI 中台的网关代理层，负责接收外部请求并转发至上游 AI 供应商，
 * 同时完成凭证认证、路由解析和用量记录等核心功能。</p>
 */
@SpringBootApplication
@MapperScan("com.aiplatform.agent.gateway.mapper")
public class AiPlatformAgentApplication {

    /**
     * 应用程序主入口方法。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AiPlatformAgentApplication.class, args);
    }
}
