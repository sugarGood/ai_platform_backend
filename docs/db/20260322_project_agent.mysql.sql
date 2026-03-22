-- ============================================================
-- AI Platform — Project Agent Schema
-- Date: 2026-03-22
--
-- 每个项目在创建时自动生成一个专属「项目智能体」。
-- 该智能体聚合项目的技能、知识库、工具，能回答所有
-- 与项目相关的问题，并支持执行发布、运维等指令。
-- ============================================================


CREATE TABLE IF NOT EXISTS project_agents (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- 关联项目
    project_id          BIGINT UNSIGNED NOT NULL                COMMENT '所属项目ID，唯一',

    -- 智能体基础信息
    name                VARCHAR(128)  NOT NULL                  COMMENT '智能体名称，默认「{项目名}助手」',
    description         TEXT          NULL                       COMMENT '智能体描述',
    avatar_icon         VARCHAR(64)   NULL                       COMMENT '头像图标（emoji 或 icon name）',

    -- 基础 System Prompt（由平台默认生成，管理员可覆盖）
    system_prompt       TEXT          NULL                       COMMENT '项目智能体全局 System Prompt',

    -- 路由配置：使用哪个 AI 模型
    preferred_model     VARCHAR(128)  NULL                       COMMENT '优先使用的模型代码，如 gpt-4o',

    -- 能力增强开关
    enable_rag          TINYINT(1)   NOT NULL DEFAULT 1          COMMENT '是否启用知识库 RAG 增强',
    enable_skills       TINYINT(1)   NOT NULL DEFAULT 1          COMMENT '是否启用项目技能注入',
    enable_tools        TINYINT(1)   NOT NULL DEFAULT 1          COMMENT '是否启用工具调用',

    -- 运维能力开关（针对「帮我发布到生产」等指令）
    enable_deploy       TINYINT(1)   NOT NULL DEFAULT 0          COMMENT '是否启用部署指令能力',
    enable_monitoring   TINYINT(1)   NOT NULL DEFAULT 0          COMMENT '是否启用监控告警查询能力',

    -- 状态
    status              ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',

    -- 元信息
    created_by          BIGINT UNSIGNED NULL                    COMMENT '创建者用户ID',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_project_agents_project (project_id),
    KEY idx_project_agents_status (status)
) COMMENT='项目专属智能体配置表';
