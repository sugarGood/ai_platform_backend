-- Agent module H2 schema (read-only refs for gateway routing)
-- Aligned with 2026-03-20 schema redesign

CREATE TABLE IF NOT EXISTS platform_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    credential_type VARCHAR(32) NOT NULL DEFAULT 'PERSONAL',
    key_hash VARCHAR(64) NOT NULL,
    key_prefix VARCHAR(32) NOT NULL,
    name VARCHAR(128),
    -- personal monthly token quota (dual-pool #1)
    monthly_token_quota BIGINT NOT NULL DEFAULT 0,
    used_tokens_this_month BIGINT NOT NULL DEFAULT 0,
    alert_threshold_pct TINYINT NOT NULL DEFAULT 80,
    over_quota_strategy VARCHAR(32) NOT NULL DEFAULT 'BLOCK',
    last_quota_reset_at TIMESTAMP,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    last_used_ip VARCHAR(45),
    revoked_at TIMESTAMP,
    revoke_reason VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_credential_key_hash UNIQUE (key_hash),
    CONSTRAINT uk_credential_user UNIQUE (user_id)
);

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
