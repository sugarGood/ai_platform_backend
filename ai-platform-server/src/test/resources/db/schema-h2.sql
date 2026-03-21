-- ============================================================================
-- AI 中台 H2 Test Schema (aligned with 2026-03-20 redesign)
-- H2 compatible: no ENUM, no UNSIGNED, no ENGINE, no CHARSET
-- ============================================================================

-- Part 1: Organization & Users
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64),
    description VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dept_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(128) NOT NULL,
    username VARCHAR(64) NOT NULL,
    full_name VARCHAR(128),
    avatar_url VARCHAR(255),
    department_id BIGINT,
    job_title VARCHAR(64),
    phone VARCHAR(20),
    platform_role VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_username UNIQUE (username)
);

-- Part 2: Projects & Members
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL,
    description TEXT,
    icon VARCHAR(32),
    project_type VARCHAR(32) NOT NULL DEFAULT 'PRODUCT',
    created_by BIGINT,
    owner_user_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS project_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_user UNIQUE (project_id, user_id)
);

CREATE TABLE IF NOT EXISTS services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    git_repo_url VARCHAR(255),
    main_branch VARCHAR(128) NOT NULL DEFAULT 'main',
    framework VARCHAR(64),
    language VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Part 3: Credentials
CREATE TABLE IF NOT EXISTS platform_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    credential_type VARCHAR(32) NOT NULL DEFAULT 'PERSONAL',
    key_hash VARCHAR(64) NOT NULL,
    key_prefix VARCHAR(32) NOT NULL,
    name VARCHAR(128),
    bound_project_id BIGINT,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    last_used_ip VARCHAR(45),
    revoked_at TIMESTAMP,
    revoke_reason VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_credential_key_hash UNIQUE (key_hash)
);

CREATE TABLE IF NOT EXISTS key_rotation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    target_label VARCHAR(128),
    rotation_type VARCHAR(16) NOT NULL DEFAULT 'MANUAL',
    old_key_prefix VARCHAR(32),
    new_key_prefix VARCHAR(32),
    result VARCHAR(16) NOT NULL,
    error_message VARCHAR(500),
    operated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Part 4: AI Providers & Models
CREATE TABLE IF NOT EXISTS ai_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    provider_type VARCHAR(32) NOT NULL,
    base_url VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_provider_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS ai_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(128) NOT NULL,
    model_family VARCHAR(64),
    context_window INT,
    input_price_per_1m DECIMAL(12,6),
    output_price_per_1m DECIMAL(12,6),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_provider_model_code UNIQUE (provider_id, code)
);

CREATE TABLE IF NOT EXISTS provider_api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    label VARCHAR(128) NOT NULL,
    key_prefix VARCHAR(32),
    api_key_encrypted VARCHAR(512) NOT NULL,
    models_allowed TEXT,
    monthly_quota_tokens BIGINT,
    used_tokens_month BIGINT NOT NULL DEFAULT 0,
    rate_limit_rpm INT,
    rate_limit_tpm INT,
    proxy_endpoint VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS provider_failover_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    primary_key_id BIGINT NOT NULL,
    fallback_key_id BIGINT NOT NULL,
    trigger_condition VARCHAR(32) NOT NULL,
    trigger_threshold VARCHAR(64),
    auto_recovery BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    last_triggered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Part 5: Knowledge Bases
CREATE TABLE IF NOT EXISTS knowledge_bases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    scope VARCHAR(16) NOT NULL,
    project_id BIGINT,
    category VARCHAR(64),
    embedding_model VARCHAR(64) NOT NULL DEFAULT 'bge-m3',
    doc_count INT NOT NULL DEFAULT 0,
    total_chunks INT NOT NULL DEFAULT 0,
    hit_rate DECIMAL(5,2),
    inject_mode VARCHAR(16) NOT NULL DEFAULT 'ON_DEMAND',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kb_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    file_type VARCHAR(32) NOT NULL,
    file_path VARCHAR(512),
    file_size BIGINT,
    chunk_count INT NOT NULL DEFAULT 0,
    hit_count INT NOT NULL DEFAULT 0,
    inject_mode VARCHAR(16) NOT NULL DEFAULT 'ON_DEMAND',
    ref_projects INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    error_message VARCHAR(500),
    uploaded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_knowledge_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    kb_id BIGINT NOT NULL,
    search_weight DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_proj_kb UNIQUE (project_id, kb_id)
);

CREATE TABLE IF NOT EXISTS knowledge_search_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kb_id BIGINT,
    project_id BIGINT,
    user_id BIGINT,
    query TEXT NOT NULL,
    search_scope VARCHAR(16) NOT NULL DEFAULT 'ALL',
    result_count INT NOT NULL DEFAULT 0,
    hit_doc_ids TEXT,
    relevance_score DECIMAL(5,4),
    latency_ms INT,
    source VARCHAR(16) NOT NULL DEFAULT 'AI_AUTO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Part 6: Skills
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_key VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    scope VARCHAR(16) NOT NULL,
    project_id BIGINT,
    category VARCHAR(32) NOT NULL DEFAULT 'OTHER',
    system_prompt TEXT,
    knowledge_refs TEXT,
    bound_tools TEXT,
    parameters TEXT,
    slash_command VARCHAR(64),
    version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    usage_count BIGINT NOT NULL DEFAULT 0,
    satisfaction_up INT NOT NULL DEFAULT 0,
    satisfaction_down INT NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    CONSTRAINT uk_skill_key UNIQUE (skill_key)
);

CREATE TABLE IF NOT EXISTS project_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_skill UNIQUE (project_id, skill_id)
);

CREATE TABLE IF NOT EXISTS skill_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    rating VARCHAR(8) NOT NULL,
    comment VARCHAR(500),
    usage_event_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Part 7: Tools
CREATE TABLE IF NOT EXISTS tool_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_name VARCHAR(64) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    description TEXT,
    scope VARCHAR(16) NOT NULL,
    project_id BIGINT,
    category VARCHAR(64) NOT NULL DEFAULT 'OTHER',
    input_schema TEXT NOT NULL,
    output_schema TEXT,
    impl_type VARCHAR(16) NOT NULL DEFAULT 'INTERNAL',
    impl_config TEXT,
    permission_required VARCHAR(64),
    audit_level VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tool_name UNIQUE (tool_name)
);

CREATE TABLE IF NOT EXISTS project_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    tool_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_tool UNIQUE (project_id, tool_id)
);

CREATE TABLE IF NOT EXISTS tool_invocation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    project_id BIGINT,
    user_id BIGINT,
    skill_id BIGINT,
    credential_id BIGINT,
    workflow_execution_id BIGINT,
    input_data TEXT,
    output_data TEXT,
    error_message VARCHAR(500),
    duration_ms INT,
    status VARCHAR(16) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Part 8: RBAC
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    role_scope VARCHAR(16) NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    default_quota_tokens BIGINT,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS permission_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(64) NOT NULL,
    permission_key VARCHAR(128) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    permission_scope VARCHAR(16) NOT NULL DEFAULT 'BOTH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_permission_key UNIQUE (permission_key)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    access_level VARCHAR(16) NOT NULL DEFAULT 'NONE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_perm UNIQUE (role_id, permission_id)
);

-- Part 9: Client Apps
CREATE TABLE IF NOT EXISTS client_apps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    icon VARCHAR(32),
    supports_mcp BOOLEAN NOT NULL DEFAULT FALSE,
    supports_custom_gateway BOOLEAN NOT NULL DEFAULT FALSE,
    setup_instruction TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_client_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS user_client_bindings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    client_app_id BIGINT NOT NULL,
    binding_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    last_active_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_client UNIQUE (user_id, client_app_id)
);

-- Part 10: Usage & Quotas
CREATE TABLE IF NOT EXISTS ai_usage_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_id BIGINT,
    user_id BIGINT,
    project_id BIGINT,
    provider_id BIGINT,
    provider_api_key_id BIGINT,
    model_id BIGINT,
    client_app_id BIGINT,
    source_type VARCHAR(32) NOT NULL,
    request_mode VARCHAR(16) NOT NULL DEFAULT 'CHAT',
    request_id VARCHAR(128),
    conversation_id VARCHAR(128),
    skill_id BIGINT,
    input_tokens BIGINT NOT NULL DEFAULT 0,
    output_tokens BIGINT NOT NULL DEFAULT 0,
    total_tokens BIGINT NOT NULL DEFAULT 0,
    cost_amount DECIMAL(16,6) NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS',
    error_message VARCHAR(500),
    latency_ms INT,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_usage_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL,
    user_id BIGINT,
    project_id BIGINT,
    provider_id BIGINT,
    model_id BIGINT,
    client_app_id BIGINT,
    total_requests INT NOT NULL DEFAULT 0,
    success_requests INT NOT NULL DEFAULT 0,
    blocked_requests INT NOT NULL DEFAULT 0,
    input_tokens BIGINT NOT NULL DEFAULT 0,
    output_tokens BIGINT NOT NULL DEFAULT 0,
    total_tokens BIGINT NOT NULL DEFAULT 0,
    cost_amount DECIMAL(16,6) NOT NULL DEFAULT 0,
    skill_invocations INT NOT NULL DEFAULT 0,
    tool_invocations INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS member_ai_quotas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    quota_type VARCHAR(32) NOT NULL,
    quota_limit BIGINT NOT NULL,
    used_amount BIGINT NOT NULL DEFAULT 0,
    reset_cycle VARCHAR(16) NOT NULL DEFAULT 'MONTHLY',
    last_reset_at TIMESTAMP,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_ai_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    policy_type VARCHAR(32) NOT NULL,
    rule_content TEXT NOT NULL,
    priority TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed default roles
MERGE INTO roles (id, name, code, role_scope, description, is_system, status) KEY (id) VALUES
    (1, '超级管理员', 'SUPER_ADMIN', 'PLATFORM', '平台超级管理员，拥有所有权限', TRUE, 'ACTIVE'),
    (2, '平台管理员', 'PLATFORM_ADMIN', 'PLATFORM', '平台管理员，管理全局配置', TRUE, 'ACTIVE'),
    (3, '普通成员', 'MEMBER', 'PLATFORM', '平台普通成员', TRUE, 'ACTIVE'),
    (4, '项目管理员', 'PROJECT_ADMIN', 'PROJECT', '项目管理员，管理项目配置和成员', TRUE, 'ACTIVE'),
    (5, '开发者', 'DEVELOPER', 'PROJECT', '项目开发者，可使用 AI 能力', TRUE, 'ACTIVE'),
    (6, '只读成员', 'VIEWER', 'PROJECT', '只读查看权限', TRUE, 'ACTIVE');

-- Seed default permission definitions
MERGE INTO permission_definitions (id, module, permission_key, name, permission_scope) KEY (id) VALUES
    (1, '知识库', 'knowledge.view', '查看知识库', 'BOTH'),
    (2, '知识库', 'knowledge.upload', '上传文档', 'BOTH'),
    (3, '知识库', 'knowledge.manage', '管理知识库', 'BOTH'),
    (4, '技能库', 'skill.view', '查看技能', 'BOTH'),
    (5, '技能库', 'skill.publish', '发布技能', 'BOTH'),
    (6, '工具集', 'tool.view', '查看工具', 'BOTH'),
    (7, '工具集', 'tool.invoke', '调用工具', 'BOTH'),
    (8, '成员管理', 'member.view', '查看成员', 'PROJECT'),
    (9, '成员管理', 'member.manage', '管理成员', 'PROJECT'),
    (10, '配额管理', 'quota.view', '查看配额', 'BOTH'),
    (11, '配额管理', 'quota.manage', '管理配额', 'BOTH'),
    (12, '项目设置', 'project.settings', '管理项目设置', 'PROJECT');
