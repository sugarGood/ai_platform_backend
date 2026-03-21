-- ============================================================
-- AI Platform Core Schema Refactor
-- Date: 2026-03-21
--
-- Business Rules (from prototype page-keys / page-projects):
--   1. One employee -> One platform credential (personal, cross-project reuse)
--   2. Employee joins projects via project_members (role per project)
--   3. Every AI call carries: credential_id + project_id
--   4. Usage is SIMULTANEOUSLY deducted from:
--        a) personal credential monthly quota  (personal pool)
--        b) project monthly token pool         (project pool)
--   5. Either pool reaching threshold triggers an alert
--   6. True API keys (provider_api_keys) never exposed to employees
-- ============================================================


-- ============================================================
-- SECTION 1: AI Provider Infrastructure
-- ============================================================

CREATE TABLE IF NOT EXISTS ai_providers (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(64)  NOT NULL,
    name          VARCHAR(128) NOT NULL,
    provider_type ENUM('OPENAI_COMPAT','ANTHROPIC','AZURE_OPENAI','BEDROCK','OTHER') NOT NULL DEFAULT 'OTHER',
    base_url      VARCHAR(255) NULL,
    status        ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ai_providers_code (code)
);

CREATE TABLE IF NOT EXISTS ai_models (
    id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    provider_id          BIGINT UNSIGNED NOT NULL,
    code                 VARCHAR(128) NOT NULL,
    name                 VARCHAR(128) NOT NULL,
    model_family         VARCHAR(64)  NULL,
    context_window       INT UNSIGNED NULL,
    input_price_per_1m   DECIMAL(12, 6) NOT NULL DEFAULT 0,
    output_price_per_1m  DECIMAL(12, 6) NOT NULL DEFAULT 0,
    status               ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ai_models_provider_code (provider_id, code),
    KEY idx_ai_models_provider_status (provider_id, status)
);

-- Upstream vendor API keys (managed by platform admin, NEVER exposed to employees)
CREATE TABLE IF NOT EXISTS provider_api_keys (
    id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    provider_id          BIGINT UNSIGNED NOT NULL,
    label                VARCHAR(128) NOT NULL,
    key_prefix           VARCHAR(32)  NULL,
    api_key_encrypted    VARCHAR(512) NOT NULL,
    models_allowed       TEXT         NULL,
    monthly_quota_tokens BIGINT UNSIGNED NOT NULL DEFAULT 0,
    rate_limit_rpm       INT UNSIGNED NULL,
    rate_limit_tpm       INT UNSIGNED NULL,
    proxy_endpoint       VARCHAR(255) NULL,
    is_fallback          TINYINT(1)  NOT NULL DEFAULT 0,
    status               ENUM('ACTIVE','DISABLED','REVOKED') NOT NULL DEFAULT 'ACTIVE',
    last_used_at         DATETIME NULL,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_provider_api_keys_provider_status (provider_id, status)
);


-- ============================================================
-- SECTION 2: Users
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    name          VARCHAR(128) NOT NULL,
    department    VARCHAR(128) NULL,
    job_type      VARCHAR(64)  NULL,
    platform_role ENUM('SUPER_ADMIN','PLATFORM_ADMIN','MEMBER') NOT NULL DEFAULT 'MEMBER',
    status        ENUM('ACTIVE','INACTIVE','DISABLED') NOT NULL DEFAULT 'INACTIVE',
    last_login_at DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_platform_role_status (platform_role, status)
);


-- ============================================================
-- SECTION 3: Platform Credentials  (ONE credential per user)
-- ============================================================
-- Generated automatically when a user is invited.
-- Reused across ALL projects the user joins - no per-project credential needed.
-- Dual-pool enforcement: every AI call checks BOTH personal quota AND project pool.

CREATE TABLE IF NOT EXISTS platform_credentials (
    id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT UNSIGNED NOT NULL,
    name                   VARCHAR(128) NOT NULL,
    key_prefix             VARCHAR(32)  NOT NULL,
    key_hash               CHAR(64)     NOT NULL,
    credential_type        ENUM('PERSONAL','SERVICE_ACCOUNT','TEMP') NOT NULL DEFAULT 'PERSONAL',
    -- Personal monthly token quota (dual-pool #1: personal pool)
    monthly_token_quota    BIGINT UNSIGNED NOT NULL DEFAULT 0
                               COMMENT 'personal monthly token limit, 0=unlimited',
    used_tokens_this_month BIGINT UNSIGNED NOT NULL DEFAULT 0
                               COMMENT 'tokens consumed this month across all projects, reset on 1st of month',
    alert_threshold_pct    TINYINT UNSIGNED NOT NULL DEFAULT 80
                               COMMENT 'alert trigger percentage (0-100)',
    over_quota_strategy    ENUM('BLOCK','ALLOW_WITH_ALERT','DOWNGRADE_MODEL') NOT NULL DEFAULT 'BLOCK',
    last_quota_reset_at    DATETIME NULL
                               COMMENT 'timestamp of last monthly quota reset',
    expires_at             DATETIME NULL,
    last_used_at           DATETIME NULL,
    last_used_ip           VARCHAR(45) NULL,
    revoked_at             DATETIME NULL,
    revoke_reason          VARCHAR(256) NULL,
    status                 ENUM('ACTIVE','DISABLED','REVOKED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform_credentials_user (user_id),
    UNIQUE KEY uk_platform_credentials_hash (key_hash),
    KEY idx_platform_credentials_status  (status),
    KEY idx_platform_credentials_expires (expires_at, status)
);

-- Role/job-type based default monthly token quotas (editable by platform admin)
CREATE TABLE IF NOT EXISTS credential_quota_policies (
    id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_type             VARCHAR(64) NOT NULL,
    monthly_token_quota  BIGINT UNSIGNED NOT NULL DEFAULT 200000,
    over_quota_strategy  ENUM('BLOCK','ALLOW_WITH_ALERT','DOWNGRADE_MODEL') NOT NULL DEFAULT 'BLOCK',
    updated_by           BIGINT UNSIGNED NULL,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_credential_quota_policies_job (job_type)
);

INSERT IGNORE INTO credential_quota_policies (job_type, monthly_token_quota) VALUES
    ('DEVELOPER', 200000),
    ('QA',        100000),
    ('PM',        100000),
    ('ADMIN',     300000),
    ('GUEST',      10000),
    ('DEFAULT',   200000);


-- ============================================================
-- SECTION 4: Projects token pool  (dual-pool #2)
-- ============================================================
-- projects table is defined in the base schema (pre-20260319).
-- These ALTER statements add the project token pool quota columns.
--
-- Project token pool rules:
--   - All members' usage within a project accumulates into used_tokens_this_month
--   - When used / quota >= alert_threshold_pct/100  ->  alert to project admin
--   - Reset to 0 on the 1st of every month at 00:00

ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS monthly_token_quota    BIGINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT 'project monthly token pool limit, 0=unlimited',
    ADD COLUMN IF NOT EXISTS used_tokens_this_month BIGINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT 'tokens consumed by all members this month, reset on 1st of month',
    ADD COLUMN IF NOT EXISTS alert_threshold_pct    TINYINT UNSIGNED NOT NULL DEFAULT 80
        COMMENT 'project pool alert trigger percentage (0-100)',
    ADD COLUMN IF NOT EXISTS over_quota_strategy    ENUM('BLOCK','ALLOW_WITH_ALERT','DOWNGRADE_MODEL') NOT NULL DEFAULT 'BLOCK'
        COMMENT 'action when project pool is exhausted',
    ADD COLUMN IF NOT EXISTS last_quota_reset_at    DATETIME NULL
        COMMENT 'timestamp of last monthly project pool reset';
