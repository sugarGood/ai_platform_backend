CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64),
    name VARCHAR(128),
    role_scope VARCHAR(32),
    status VARCHAR(32),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(128),
    username VARCHAR(64),
    full_name VARCHAR(128),
    avatar_url VARCHAR(255),
    department_id BIGINT,
    job_title VARCHAR(128),
    phone VARCHAR(64),
    role_id BIGINT,
    platform_role VARCHAR(64),
    password_hash VARCHAR(255),
    status VARCHAR(32),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permission_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(64),
    permission_key VARCHAR(128),
    name VARCHAR(128),
    description VARCHAR(255),
    permission_scope VARCHAR(32),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rbac_role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(64),
    permission_code VARCHAR(128),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(64),
    project_type VARCHAR(32),
    created_by BIGINT,
    owner_user_id BIGINT,
    monthly_token_quota BIGINT,
    used_tokens_this_month BIGINT,
    alert_threshold_pct INT,
    over_quota_strategy VARCHAR(32),
    last_quota_reset_at TIMESTAMP,
    quota_reset_cycle VARCHAR(32),
    single_request_token_cap BIGINT,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_agents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(128),
    description CLOB,
    avatar_icon VARCHAR(64),
    system_prompt CLOB,
    preferred_model VARCHAR(128),
    enable_rag BOOLEAN,
    enable_skills BOOLEAN,
    enable_tools BOOLEAN,
    enable_deploy BOOLEAN,
    enable_monitoring BOOLEAN,
    status VARCHAR(32),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(32),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT,
    name VARCHAR(128),
    code VARCHAR(128),
    description VARCHAR(500),
    service_type VARCHAR(64),
    owner_user_id BIGINT,
    repository_url VARCHAR(255),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS platform_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bound_project_id BIGINT,
    credential_type VARCHAR(32),
    name VARCHAR(128),
    key_prefix VARCHAR(32),
    key_hash VARCHAR(64),
    monthly_token_quota BIGINT,
    used_tokens_this_month BIGINT,
    alert_threshold_pct INT,
    over_quota_strategy VARCHAR(32),
    last_quota_reset_at TIMESTAMP,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    last_used_ip VARCHAR(45),
    revoked_at TIMESTAMP,
    revoke_reason VARCHAR(255),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS member_ai_quotas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT,
    quota_type VARCHAR(32),
    quota_limit BIGINT,
    used_amount BIGINT,
    reset_cycle VARCHAR(32),
    last_reset_at TIMESTAMP,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_usage_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_id BIGINT,
    project_id BIGINT,
    user_id BIGINT,
    provider_id BIGINT,
    provider_api_key_id BIGINT,
    model_id BIGINT,
    client_app_id BIGINT,
    skill_id BIGINT,
    source_type VARCHAR(64),
    request_mode VARCHAR(32),
    request_id VARCHAR(128),
    conversation_id VARCHAR(128),
    input_tokens BIGINT,
    output_tokens BIGINT,
    total_tokens BIGINT,
    cost_amount DECIMAL(16, 6),
    quota_check_result VARCHAR(64),
    status VARCHAR(32),
    error_message VARCHAR(500),
    latency_ms INT,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    severity VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS alert_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT,
    project_id BIGINT,
    user_id BIGINT,
    trigger_value VARCHAR(128),
    message VARCHAR(500),
    notified_channels VARCHAR(255),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_bases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT,
    name VARCHAR(128),
    scope VARCHAR(32),
    inject_mode VARCHAR(32),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_knowledge_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    kb_id BIGINT NOT NULL,
    inject_mode VARCHAR(32),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    tool_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
