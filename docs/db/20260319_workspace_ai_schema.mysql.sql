-- Workspace-oriented AI gateway schema for ai_platform
-- Date: 2026-03-19
-- Scope:
--   1. Split a project into multiple workspaces.
--   2. Allow different workspaces to use different AI capabilities.
--   3. Assign gateway credentials to workspace members.
--   4. Reuse provider_api_keys as upstream vendor secrets.
--   5. Extend audit and usage tables so quota enforcement can be traced to workspace and credential.

CREATE TABLE IF NOT EXISTS project_workspaces (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL,
    workspace_type ENUM('R_AND_D', 'OPS', 'TEST', 'PRODUCT', 'DATA', 'DESIGN', 'SECURITY', 'OTHER') NOT NULL DEFAULT 'OTHER',
    description VARCHAR(500) NULL,
    default_provider_id BIGINT UNSIGNED NULL,
    default_model_id BIGINT UNSIGNED NULL,
    status ENUM('ACTIVE', 'DISABLED', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_project_workspaces_project_code (project_id, code),
    UNIQUE KEY uk_project_workspaces_project_name (project_id, name),
    KEY idx_project_workspaces_project_status (project_id, status),
    KEY idx_project_workspaces_type_status (workspace_type, status)
);

CREATE TABLE IF NOT EXISTS ai_capabilities (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    capability_type ENUM('CHAT', 'CODE', 'TEST', 'OPS', 'PRODUCT', 'DATA', 'AGENT', 'EMBEDDING', 'MCP_TOOL', 'OTHER') NOT NULL DEFAULT 'OTHER',
    request_mode ENUM('CHAT', 'CODE', 'MCP_TOOL', 'EMBEDDING', 'OTHER') NOT NULL DEFAULT 'OTHER',
    description VARCHAR(500) NULL,
    status ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ai_capabilities_code (code),
    KEY idx_ai_capabilities_type_status (capability_type, status)
);

CREATE TABLE IF NOT EXISTS project_workspace_members (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT UNSIGNED NOT NULL,
    project_id BIGINT UNSIGNED NOT NULL,
    project_member_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    member_role ENUM('WORKSPACE_OWNER', 'LEAD', 'MEMBER', 'VIEWER') NOT NULL DEFAULT 'MEMBER',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_workspace_members_workspace_user (workspace_id, user_id),
    UNIQUE KEY uk_workspace_members_workspace_project_member (workspace_id, project_member_id),
    KEY idx_workspace_members_project_user (project_id, user_id),
    KEY idx_workspace_members_workspace_status (workspace_id, status)
);

CREATE TABLE IF NOT EXISTS workspace_ai_capabilities (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT UNSIGNED NOT NULL,
    ai_capability_id BIGINT UNSIGNED NOT NULL,
    -- 0 means wildcard: any provider or any model that matches the capability.
    provider_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
    model_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
    monthly_request_quota INT UNSIGNED NOT NULL DEFAULT 0,
    monthly_token_quota BIGINT UNSIGNED NOT NULL DEFAULT 0,
    monthly_cost_quota DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    over_quota_strategy ENUM('BLOCK', 'ALLOW_WITH_ALERT', 'DOWNGRADE_MODEL') NOT NULL DEFAULT 'BLOCK',
    status ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_workspace_ai_capabilities_scope (workspace_id, ai_capability_id, provider_id, model_id),
    KEY idx_workspace_ai_capabilities_workspace_status (workspace_id, status),
    KEY idx_workspace_ai_capabilities_capability_status (ai_capability_id, status)
);

CREATE TABLE IF NOT EXISTS workspace_ai_credentials (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(128) NOT NULL,
    credential_code VARCHAR(64) NOT NULL,
    credential_type ENUM('PERSONAL', 'SHARED', 'SERVICE_ACCOUNT') NOT NULL DEFAULT 'PERSONAL',
    provider_api_key_id BIGINT UNSIGNED NOT NULL,
    default_model_id BIGINT UNSIGNED NULL,
    monthly_request_quota INT UNSIGNED NOT NULL DEFAULT 0,
    monthly_token_quota BIGINT UNSIGNED NOT NULL DEFAULT 0,
    monthly_cost_quota DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    alert_threshold_percent TINYINT UNSIGNED NOT NULL DEFAULT 80,
    over_quota_strategy ENUM('BLOCK', 'ALLOW_WITH_ALERT', 'DOWNGRADE_MODEL') NOT NULL DEFAULT 'BLOCK',
    audit_enabled TINYINT(1) NOT NULL DEFAULT 1,
    expires_at DATETIME NULL,
    last_used_at DATETIME NULL,
    status ENUM('ACTIVE', 'DISABLED', 'REVOKED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_workspace_ai_credentials_workspace_code (workspace_id, credential_code),
    UNIQUE KEY uk_workspace_ai_credentials_workspace_name (workspace_id, name),
    KEY idx_workspace_ai_credentials_workspace_status (workspace_id, status),
    KEY idx_workspace_ai_credentials_provider_key (provider_api_key_id)
);

CREATE TABLE IF NOT EXISTS workspace_member_ai_credentials (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    workspace_member_id BIGINT UNSIGNED NOT NULL,
    workspace_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    workspace_ai_credential_id BIGINT UNSIGNED NOT NULL,
    assignment_type ENUM('PRIMARY', 'SECONDARY') NOT NULL DEFAULT 'PRIMARY',
    -- If override fields are null, quota falls back to workspace_ai_credentials defaults.
    monthly_request_quota_override INT UNSIGNED NULL,
    monthly_token_quota_override BIGINT UNSIGNED NULL,
    monthly_cost_quota_override DECIMAL(14, 2) NULL,
    bound_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NULL,
    last_used_at DATETIME NULL,
    status ENUM('ACTIVE', 'DISABLED', 'REVOKED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_workspace_member_ai_credentials_member_credential (workspace_member_id, workspace_ai_credential_id),
    KEY idx_workspace_member_ai_credentials_workspace_user (workspace_id, user_id),
    KEY idx_workspace_member_ai_credentials_credential_status (workspace_ai_credential_id, status)
);

CREATE TABLE IF NOT EXISTS platform_access_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT UNSIGNED NULL,
    user_id BIGINT UNSIGNED NULL,
    client_app_id BIGINT UNSIGNED NULL,
    workspace_id BIGINT UNSIGNED NULL,
    workspace_member_id BIGINT UNSIGNED NULL,
    workspace_member_ai_credential_id BIGINT UNSIGNED NULL,
    provider_api_key_id BIGINT UNSIGNED NULL,
    project_member_id BIGINT UNSIGNED NULL,
    name VARCHAR(128) NOT NULL,
    role_snapshot VARCHAR(32) NOT NULL,
    token_prefix VARCHAR(32) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    allowed_capability_codes VARCHAR(500) NULL,
    expires_at DATETIME NOT NULL,
    last_used_at DATETIME NULL,
    status ENUM('ACTIVE', 'DISABLED', 'REVOKED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform_access_tokens_token_hash (token_hash),
    KEY idx_platform_access_tokens_project_member_id (project_member_id),
    KEY idx_platform_access_tokens_workspace_id (workspace_id),
    KEY idx_platform_access_tokens_workspace_member_id (workspace_member_id),
    KEY idx_platform_access_tokens_member_credential_id (workspace_member_ai_credential_id),
    KEY idx_platform_access_tokens_provider_api_key_id (provider_api_key_id)
);

ALTER TABLE platform_access_tokens
    ADD COLUMN IF NOT EXISTS workspace_id BIGINT UNSIGNED NULL AFTER project_id,
    ADD COLUMN IF NOT EXISTS workspace_member_id BIGINT UNSIGNED NULL AFTER user_id,
    ADD COLUMN IF NOT EXISTS workspace_member_ai_credential_id BIGINT UNSIGNED NULL AFTER client_app_id,
    ADD COLUMN IF NOT EXISTS provider_api_key_id BIGINT UNSIGNED NULL AFTER workspace_member_ai_credential_id,
    ADD KEY idx_platform_access_tokens_workspace_id (workspace_id),
    ADD KEY idx_platform_access_tokens_workspace_member_id (workspace_member_id),
    ADD KEY idx_platform_access_tokens_member_credential_id (workspace_member_ai_credential_id),
    ADD KEY idx_platform_access_tokens_provider_api_key_id (provider_api_key_id);

ALTER TABLE ai_usage_events
    ADD COLUMN IF NOT EXISTS workspace_id BIGINT UNSIGNED NULL AFTER project_id,
    ADD COLUMN IF NOT EXISTS workspace_member_id BIGINT UNSIGNED NULL AFTER user_id,
    ADD COLUMN IF NOT EXISTS workspace_member_ai_credential_id BIGINT UNSIGNED NULL AFTER client_app_id,
    ADD COLUMN IF NOT EXISTS provider_api_key_id BIGINT UNSIGNED NULL AFTER provider_id,
    ADD COLUMN IF NOT EXISTS ai_capability_id BIGINT UNSIGNED NULL AFTER model_id,
    ADD COLUMN IF NOT EXISTS platform_access_token_id BIGINT UNSIGNED NULL AFTER ai_capability_id,
    ADD KEY idx_ai_usage_events_workspace_id (workspace_id),
    ADD KEY idx_ai_usage_events_workspace_member_id (workspace_member_id),
    ADD KEY idx_ai_usage_events_member_credential_id (workspace_member_ai_credential_id),
    ADD KEY idx_ai_usage_events_ai_capability_id (ai_capability_id),
    ADD KEY idx_ai_usage_events_platform_access_token_id (platform_access_token_id);

ALTER TABLE ai_usage_daily
    ADD COLUMN IF NOT EXISTS workspace_id BIGINT UNSIGNED NULL AFTER project_id,
    ADD COLUMN IF NOT EXISTS workspace_member_id BIGINT UNSIGNED NULL AFTER user_id,
    ADD COLUMN IF NOT EXISTS workspace_member_ai_credential_id BIGINT UNSIGNED NULL AFTER client_app_id,
    ADD COLUMN IF NOT EXISTS ai_capability_id BIGINT UNSIGNED NULL AFTER model_id,
    ADD KEY idx_ai_usage_daily_workspace_id (workspace_id),
    ADD KEY idx_ai_usage_daily_workspace_member_id (workspace_member_id),
    ADD KEY idx_ai_usage_daily_member_credential_id (workspace_member_ai_credential_id),
    ADD KEY idx_ai_usage_daily_ai_capability_id (ai_capability_id);

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS workspace_id BIGINT UNSIGNED NULL AFTER project_id,
    ADD COLUMN IF NOT EXISTS workspace_member_id BIGINT UNSIGNED NULL AFTER workspace_id,
    ADD COLUMN IF NOT EXISTS workspace_member_ai_credential_id BIGINT UNSIGNED NULL AFTER workspace_member_id,
    ADD COLUMN IF NOT EXISTS platform_access_token_id BIGINT UNSIGNED NULL AFTER workspace_member_ai_credential_id,
    ADD COLUMN IF NOT EXISTS provider_api_key_id BIGINT UNSIGNED NULL AFTER platform_access_token_id,
    ADD COLUMN IF NOT EXISTS request_id VARCHAR(128) NULL AFTER action,
    ADD KEY idx_audit_logs_workspace_id (workspace_id),
    ADD KEY idx_audit_logs_workspace_member_id (workspace_member_id),
    ADD KEY idx_audit_logs_member_credential_id (workspace_member_ai_credential_id),
    ADD KEY idx_audit_logs_platform_access_token_id (platform_access_token_id),
    ADD KEY idx_audit_logs_request_id (request_id);

INSERT IGNORE INTO ai_capabilities (code, name, capability_type, request_mode, description)
VALUES
    ('GENERAL_CHAT', 'General Chat', 'CHAT', 'CHAT', 'General conversational assistance for workspace members.'),
    ('CODE_GENERATION', 'Code Generation', 'CODE', 'CODE', 'Generate, refactor, and explain code for engineering teams.'),
    ('CODE_REVIEW', 'Code Review', 'CODE', 'CODE', 'Analyze diffs and provide engineering review suggestions.'),
    ('TEST_CASE_GENERATION', 'Test Case Generation', 'TEST', 'CODE', 'Generate test cases, test scripts, and validation plans.'),
    ('TEST_REPORT_ANALYSIS', 'Test Report Analysis', 'TEST', 'CHAT', 'Summarize test reports and suggest regression focus areas.'),
    ('OPS_DIAGNOSIS', 'Ops Diagnosis', 'OPS', 'CHAT', 'Analyze logs, incidents, and deployment symptoms.'),
    ('RUNBOOK_ASSISTANT', 'Runbook Assistant', 'OPS', 'MCP_TOOL', 'Call approved tools and execute standard operational runbooks.'),
    ('PRD_DRAFTING', 'PRD Drafting', 'PRODUCT', 'CHAT', 'Draft product requirements, release notes, and feature briefs.'),
    ('DATA_QUERY_ASSISTANT', 'Data Query Assistant', 'DATA', 'CODE', 'Generate and explain SQL and analytics queries.');
