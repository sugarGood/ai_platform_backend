-- =============================================================================
-- 闭环补丁（通用）：为库内所有「尚无平台凭证」的 ACTIVE 用户补凭证，并补全项目成员关系
-- =============================================================================
-- 适用：已有 users / projects 表及数据；执行一次即可，可重复执行（幂等）。
--
-- 凭证规则（与 PlatformCredentialService 一致）：
--   明文 = CONCAT('plt_', user_id, '_', 16位小写hex(user_id), '_', chr(97 + user_id % 26))
--   key_hash = SHA256(明文)，UTF-8
--   key_prefix = 明文前 12 字符
--
-- 示例：user_id=7 -> plt_7_0000000000000007_h
--       Authorization: Bearer plt_7_0000000000000007_h
--
-- 项目成员：每个 ACTIVE 用户 × 每个 ACTIVE 项目；uk_project_user 已存在则 INSERT IGNORE 跳过
-- =============================================================================

START TRANSACTION;

-- 1) 为尚无 platform_credentials 的用户各插入一条 PERSONAL（MySQL 8+ SHA2）
INSERT INTO platform_credentials (
    user_id, credential_type, key_hash, key_prefix, name,
    monthly_token_quota, used_tokens_this_month, alert_threshold_pct, over_quota_strategy,
    last_quota_reset_at, status
)
SELECT u.id,
       'PERSONAL',
       SHA2(
               CONCAT(
                       'plt_',
                       u.id,
                       '_',
                       LPAD(LOWER(HEX(u.id)), 16, '0'),
                       '_',
                       CHAR(97 + (u.id % 26))
               ),
               256
       ),
       LEFT(
               CONCAT(
                       'plt_',
                       u.id,
                       '_',
                       LPAD(LOWER(HEX(u.id)), 16, '0'),
                       '_',
                       CHAR(97 + (u.id % 26))
               ),
               12
       ),
       CONCAT(IFNULL(NULLIF(TRIM(u.full_name), ''), u.username), '-个人凭证（闭环补丁）'),
       0,
       0,
       80,
       'BLOCK',
       NOW(),
       'ACTIVE'
FROM users u
WHERE u.status = 'ACTIVE'
  AND NOT EXISTS (SELECT 1 FROM platform_credentials c WHERE c.user_id = u.id);

-- 2) 全员加入全部 ACTIVE 项目（已有关系不覆盖）
INSERT IGNORE INTO project_members (project_id, user_id, role, joined_at)
SELECT p.id,
       u.id,
       CASE
           WHEN u.platform_role IN ('SUPER_ADMIN', 'PLATFORM_ADMIN') THEN 'ADMIN'
           ELSE 'MEMBER'
           END,
       NOW()
FROM projects p
         CROSS JOIN users u
WHERE p.status = 'ACTIVE'
  AND u.status = 'ACTIVE';

COMMIT;
