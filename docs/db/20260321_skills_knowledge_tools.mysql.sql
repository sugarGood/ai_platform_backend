-- ============================================================
-- AI Platform - Skills & Knowledge Base Schema
-- Date: 2026-03-21
--
-- Adds tables for:
--   1. skills                     - AI 技能定义（含 system prompt、知识库引用、工具绑定）
--   2. project_skills             - 项目启用的技能关联
--   3. knowledge_bases            - 知识库定义（支持 RAG 检索增强）
--   4. project_knowledge_configs  - 项目关联的知识库配置（含检索权重）
--   5. tool_definitions           - 工具定义（AI 可调用的外部能力）
--   6. project_tools              - 项目启用的工具关联
-- ============================================================


-- ============================================================
-- SECTION 1: Skills
-- ============================================================

CREATE TABLE IF NOT EXISTS skills (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_key         VARCHAR(64)  NOT NULL                  COMMENT '技能唯一标识（英文）',
    name              VARCHAR(128) NOT NULL                  COMMENT '技能名称',
    description       TEXT         NULL                      COMMENT '技能描述',
    scope             ENUM('GLOBAL','PROJECT') NOT NULL      COMMENT '作用域：GLOBAL=全局 / PROJECT=项目级',
    project_id        BIGINT UNSIGNED NULL                   COMMENT '所属项目ID，scope=PROJECT 时有效',
    category          VARCHAR(32)  NOT NULL DEFAULT 'OTHER'  COMMENT '分类：ENGINEERING / QUALITY / SECURITY 等',
    system_prompt     TEXT         NULL                      COMMENT '角色 Prompt 模板',
    knowledge_refs    TEXT         NULL                      COMMENT '关联知识库引用（JSON）',
    bound_tools       TEXT         NULL                      COMMENT '绑定工具列表（JSON）',
    parameters        TEXT         NULL                      COMMENT '用户可配参数（JSON）',
    slash_command     VARCHAR(64)  NULL                      COMMENT '斜杠触发命令，如 /code-review',
    version           VARCHAR(16)  NOT NULL DEFAULT '1.0.0'  COMMENT '版本号',
    status            ENUM('DRAFT','PUBLISHED','DEPRECATED') NOT NULL DEFAULT 'DRAFT',
    usage_count       BIGINT UNSIGNED NOT NULL DEFAULT 0     COMMENT '累计使用次数',
    satisfaction_up   INT UNSIGNED    NOT NULL DEFAULT 0     COMMENT '正反馈数（点赞）',
    satisfaction_down INT UNSIGNED    NOT NULL DEFAULT 0     COMMENT '负反馈数（点踩）',
    created_by        BIGINT UNSIGNED NULL                   COMMENT '创建者用户ID',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at      DATETIME NULL,
    UNIQUE KEY uk_skills_key (skill_key),
    KEY idx_skills_scope_status (scope, status),
    KEY idx_skills_project (project_id)
) COMMENT='AI 技能定义表';


-- ============================================================
-- SECTION 2: Project-Skill Association
-- ============================================================

CREATE TABLE IF NOT EXISTS project_skills (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT UNSIGNED NOT NULL              COMMENT '项目ID',
    skill_id   BIGINT UNSIGNED NOT NULL              COMMENT '技能ID',
    status     ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_project_skill (project_id, skill_id),
    KEY idx_project_skills_skill (skill_id)
) COMMENT='项目启用的技能关联表';


-- ============================================================
-- SECTION 3: Knowledge Bases
-- ============================================================

CREATE TABLE IF NOT EXISTS knowledge_bases (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(128) NOT NULL                       COMMENT '知识库名称',
    description     TEXT         NULL                           COMMENT '知识库描述',
    scope           ENUM('GLOBAL','PROJECT') NOT NULL           COMMENT '作用域：GLOBAL=全局共享 / PROJECT=项目私有',
    project_id      BIGINT UNSIGNED NULL                        COMMENT '所属项目ID，scope=PROJECT 时有效',
    category        VARCHAR(64)  NULL                           COMMENT '分类',
    embedding_model VARCHAR(64)  NOT NULL DEFAULT 'bge-m3'      COMMENT '向量化模型',
    doc_count       INT UNSIGNED NOT NULL DEFAULT 0             COMMENT '文档数量',
    total_chunks    INT UNSIGNED NOT NULL DEFAULT 0             COMMENT '总知识块数',
    hit_rate        DECIMAL(5,2) NULL                           COMMENT '检索命中率（%）',
    inject_mode     ENUM('AUTO_INJECT','ON_DEMAND','DISABLED') NOT NULL DEFAULT 'ON_DEMAND'
                                                                COMMENT '注入模式：AUTO_INJECT=自动 / ON_DEMAND=按需 / DISABLED=禁用',
    status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by      BIGINT UNSIGNED NULL                        COMMENT '创建者用户ID',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_knowledge_bases_scope_status (scope, status),
    KEY idx_knowledge_bases_project (project_id)
) COMMENT='知识库定义表';


-- ============================================================
-- SECTION 4: Project-Knowledge Base Configuration
-- ============================================================

CREATE TABLE IF NOT EXISTS project_knowledge_configs (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT UNSIGNED NOT NULL                     COMMENT '项目ID',
    kb_id         BIGINT UNSIGNED NOT NULL                     COMMENT '全局知识库ID',
    search_weight DECIMAL(3,2)   NOT NULL DEFAULT 1.00         COMMENT '检索权重（0~1）',
    status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_proj_kb (project_id, kb_id),
    KEY idx_project_knowledge_configs_kb (kb_id)
) COMMENT='项目知识库配置表';


-- ============================================================
-- SECTION 5: Tool Definitions
-- ============================================================

CREATE TABLE IF NOT EXISTS tool_definitions (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tool_name           VARCHAR(64)  NOT NULL                  COMMENT '工具唯一标识（AI 调用名）',
    display_name        VARCHAR(128) NOT NULL                  COMMENT '显示名称',
    description         TEXT         NULL                      COMMENT '工具描述',
    scope               ENUM('BUILTIN','GLOBAL','PROJECT') NOT NULL COMMENT '作用域',
    project_id          BIGINT UNSIGNED NULL                   COMMENT '所属项目ID，scope=PROJECT 时有效',
    category            VARCHAR(64)  NOT NULL DEFAULT 'OTHER'  COMMENT '工具分类',
    input_schema        TEXT         NOT NULL                  COMMENT '输入参数 JSON Schema',
    output_schema       TEXT         NULL                      COMMENT '输出结果 JSON Schema',
    impl_type           ENUM('INTERNAL','HTTP_CALLBACK','MCP_PROXY') NOT NULL DEFAULT 'INTERNAL'
                                                               COMMENT '实现类型',
    impl_config         TEXT         NULL                      COMMENT '实现配置（JSON，因 impl_type 而异）',
    permission_required VARCHAR(64)  NULL                      COMMENT '调用所需权限',
    audit_level         ENUM('NORMAL','SENSITIVE','CRITICAL') NOT NULL DEFAULT 'NORMAL'
                                                               COMMENT '审计级别',
    status              ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tool_definitions_name (tool_name),
    KEY idx_tool_definitions_scope_status (scope, status),
    KEY idx_tool_definitions_project (project_id)
) COMMENT='工具定义表';


-- ============================================================
-- SECTION 6: Project-Tool Association
-- ============================================================

CREATE TABLE IF NOT EXISTS project_tools (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT UNSIGNED NOT NULL              COMMENT '项目ID',
    tool_id    BIGINT UNSIGNED NOT NULL              COMMENT '工具ID',
    status     ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_project_tool (project_id, tool_id),
    KEY idx_project_tools_tool (tool_id)
) COMMENT='项目启用的工具关联表';
