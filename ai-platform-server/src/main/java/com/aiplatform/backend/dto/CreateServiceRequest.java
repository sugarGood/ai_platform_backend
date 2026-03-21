package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建项目服务的请求参数。
 *
 * @param name        服务名称（必填）
 * @param description 服务描述
 * @param gitRepoUrl  Git 仓库地址
 * @param mainBranch  主分支名称（默认 "main"）
 * @param framework   技术框架
 * @param language    编程语言
 */
public record CreateServiceRequest(
        @NotBlank
        String name,
        String description,
        String gitRepoUrl,
        String mainBranch,
        String framework,
        String language
) {
}
