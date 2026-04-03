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
    description VARCHAR(500),
    git_repo_url VARCHAR(255),
    main_branch VARCHAR(128),
    framework VARCHAR(128),
    language VARCHAR(64),
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
    search_weight DECIMAL(5, 2),
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

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_key VARCHAR(128),
    name VARCHAR(128),
    description CLOB,
    scope VARCHAR(32),
    project_id BIGINT,
    category VARCHAR(64),
    system_prompt CLOB,
    knowledge_refs CLOB,
    bound_tools CLOB,
    parameters CLOB,
    slash_command VARCHAR(128),
    version VARCHAR(32),
    status VARCHAR(32),
    usage_count BIGINT,
    satisfaction_up INT,
    satisfaction_down INT,
    created_by BIGINT,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tool_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_name VARCHAR(128),
    display_name VARCHAR(128),
    description CLOB,
    scope VARCHAR(32),
    project_id BIGINT,
    category VARCHAR(64),
    input_schema CLOB,
    output_schema CLOB,
    impl_type VARCHAR(64),
    impl_config CLOB,
    permission_required VARCHAR(128),
    audit_level VARCHAR(32),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kb_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    title VARCHAR(255),
    file_type VARCHAR(32),
    file_path VARCHAR(500),
    file_size BIGINT,
    chunk_count INT,
    hit_count INT,
    inject_mode VARCHAR(32),
    ref_projects INT,
    status VARCHAR(32),
    error_message CLOB,
    uploaded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mcp_servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_name VARCHAR(128),
    display_name VARCHAR(128),
    description CLOB,
    server_type VARCHAR(32),
    project_id BIGINT,
    category VARCHAR(64),
    server_url VARCHAR(255),
    auth_type VARCHAR(32),
    auth_config CLOB,
    capabilities CLOB,
    status VARCHAR(32),
    last_checked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_mcp_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    mcp_server_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tool_invocation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    project_id BIGINT,
    request_payload CLOB,
    response_payload CLOB,
    status VARCHAR(32),
    duration_ms INT,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_member_permission_overrides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_member_id BIGINT NOT NULL,
    module_key VARCHAR(64) NOT NULL,
    access_level VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_member_resource_grants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_member_id BIGINT NOT NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_id BIGINT NOT NULL,
    grant_level VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_role_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    role_code VARCHAR(32) NOT NULL,
    template_name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_role_template_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_role_template_id BIGINT NOT NULL,
    module_key VARCHAR(64) NOT NULL,
    access_level VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_atomic_capabilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    atomic_capability_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS client_apps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64),
    name VARCHAR(128),
    icon VARCHAR(64),
    supports_mcp BOOLEAN,
    supports_custom_gateway BOOLEAN,
    setup_instruction CLOB,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_client_bindings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    client_app_id BIGINT NOT NULL,
    binding_status VARCHAR(32),
    last_active_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS key_rotation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(64),
    target_id BIGINT,
    target_label VARCHAR(128),
    rotation_type VARCHAR(32),
    old_key_prefix VARCHAR(64),
    new_key_prefix VARCHAR(64),
    result VARCHAR(32),
    error_message VARCHAR(255),
    operated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT,
    user_id BIGINT,
    actor_name VARCHAR(64),
    action_type VARCHAR(64),
    summary VARCHAR(500),
    target_type VARCHAR(64),
    target_id BIGINT,
    target_name VARCHAR(128),
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS atomic_capabilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128),
    code VARCHAR(128),
    description VARCHAR(255),
    icon VARCHAR(64),
    category VARCHAR(64),
    doc_content CLOB,
    api_spec_url VARCHAR(255),
    git_repo_url VARCHAR(255),
    version VARCHAR(32),
    supported_languages VARCHAR(128),
    subscription_count INT,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mcp_servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_name VARCHAR(128),
    display_name VARCHAR(128),
    description VARCHAR(255),
    server_type VARCHAR(32),
    project_id BIGINT,
    category VARCHAR(64),
    server_url VARCHAR(255),
    auth_type VARCHAR(32),
    auth_config CLOB,
    capabilities CLOB,
    status VARCHAR(32),
    last_checked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_mcp_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    mcp_server_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_key VARCHAR(128),
    name VARCHAR(128),
    description VARCHAR(255),
    scope VARCHAR(32),
    project_id BIGINT,
    category VARCHAR(64),
    system_prompt CLOB,
    knowledge_refs CLOB,
    bound_tools CLOB,
    parameters CLOB,
    slash_command VARCHAR(128),
    version VARCHAR(32),
    status VARCHAR(32),
    usage_count BIGINT,
    satisfaction_up INT,
    satisfaction_down INT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tool_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_name VARCHAR(128),
    display_name VARCHAR(128),
    description VARCHAR(255),
    scope VARCHAR(32),
    project_id BIGINT,
    category VARCHAR(64),
    input_schema CLOB,
    output_schema CLOB,
    impl_type VARCHAR(32),
    impl_config CLOB,
    permission_required VARCHAR(128),
    audit_level VARCHAR(32),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

