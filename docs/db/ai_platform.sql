/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.0.11
 Source Server Type    : MySQL
 Source Server Version : 80300 (8.3.0)
 Source Host           : 192.168.0.11:3306
 Source Schema         : ai_platform

 Target Server Type    : MySQL
 Target Server Version : 80300 (8.3.0)
 File Encoding         : 65001

 Date: 30/03/2026 10:53:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_logs
-- ----------------------------
DROP TABLE IF EXISTS `activity_logs`;
CREATE TABLE `activity_logs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联项目 ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '操作用户 ID',
  `actor_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作者名称（冗余）',
  `action_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作类型',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动摘要',
  `target_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标对象类型',
  `target_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '目标对象 ID',
  `target_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标对象名称',
  `occurred_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_occurred`(`occurred_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动日志表（时间线）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of activity_logs
-- ----------------------------
INSERT INTO `activity_logs` VALUES (1, 1, 11, '谢琳', 'kb.reindex', '完成《RAG badcase 汇总》重建索引任务', 'kb_document', 5, 'RAG 评测 badcase 汇总', '2026-03-18 16:10:00', '2026-03-20 23:45:00');
INSERT INTO `activity_logs` VALUES (2, 3, 5, '赵磊', 'deploy', 'dev-copilot-gateway 发布 v0.9.2 至预发', 'service', 7, 'dev-copilot-gateway', '2026-03-17 21:40:00', '2026-03-20 23:45:00');
INSERT INTO `activity_logs` VALUES (3, 2, 7, '周浩', 'nl2sql.query', '高管看板：华东区近 7 日履约准时率', 'report', NULL, '准时率周报', '2026-03-20 09:58:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for ai_eval_scores
-- ----------------------------
DROP TABLE IF EXISTS `ai_eval_scores`;
CREATE TABLE `ai_eval_scores`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `eval_target_type` enum('WORKFLOW','SKILL','MODEL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评估目标类型',
  `eval_target_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '评估目标 ID',
  `eval_target_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评估目标名称',
  `eval_period` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评估周期（如 2026-W12）',
  `overall_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '综合分数',
  `grade` enum('A','B','C','D','F') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评级',
  `detail_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评估详情（JSON）',
  `improvement_suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '改进建议',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target`(`eval_target_type` ASC, `eval_target_id` ASC) USING BTREE,
  INDEX `idx_period`(`eval_period` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 评估分数表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_eval_scores
-- ----------------------------
INSERT INTO `ai_eval_scores` VALUES (1, 'SKILL', 1, '工单摘要', '2026-W11', 86.50, 'B', '{\"dimensions\":{\"accuracy\":88,\"brevity\":85}}', '加强多轮对话上下文的指代消解', '2026-03-17 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_eval_scores` VALUES (2, 'MODEL', 3, 'Claude Sonnet 4', '2026-03', 91.20, 'A', '{\"latency_p95_ms\":980}', '保持当前路由权重', '2026-03-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for ai_models
-- ----------------------------
DROP TABLE IF EXISTS `ai_models`;
CREATE TABLE `ai_models`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id` bigint UNSIGNED NOT NULL COMMENT '供应商 ID',
  `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型标识（如 claude-opus-4-6）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型展示名',
  `model_family` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型系列（如 Claude 4）',
  `context_window` int UNSIGNED NULL DEFAULT NULL COMMENT '上下文窗口大小',
  `input_price_per_1m` decimal(12, 6) NULL DEFAULT NULL COMMENT '输入每百万 Token 价格（USD）',
  `output_price_per_1m` decimal(12, 6) NULL DEFAULT NULL COMMENT '输出每百万 Token 价格（USD）',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_provider_code`(`provider_id` ASC, `code` ASC) USING BTREE,
  INDEX `idx_family`(`model_family` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 模型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_models
-- ----------------------------
INSERT INTO `ai_models` VALUES (1, 1, 'gpt-4.1', 'GPT-4.1', 'GPT-4', 128000, 2.000000, 8.000000, 'ACTIVE', '2025-01-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_models` VALUES (2, 1, 'gpt-4.1-mini', 'GPT-4.1 Mini', 'GPT-4', 128000, 0.400000, 1.600000, 'ACTIVE', '2025-01-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_models` VALUES (3, 2, 'claude-sonnet-4-20250514', 'Claude Sonnet 4', 'Claude 4', 200000, 3.000000, 15.000000, 'ACTIVE', '2025-01-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_models` VALUES (4, 2, 'claude-3-5-haiku-20241022', 'Claude 3.5 Haiku', 'Claude 3.5', 200000, 0.800000, 4.000000, 'ACTIVE', '2025-01-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_models` VALUES (5, 3, 'deepseek-chat', 'DeepSeek Chat', 'DeepSeek V3', 64000, 0.270000, 1.100000, 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_models` VALUES (6, 4, 'Qwen/Qwen2.5-72B-Instruct', 'Qwen2.5 72B', 'Qwen2.5', 32768, 0.350000, 0.350000, 'ACTIVE', '2025-04-10 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for ai_providers
-- ----------------------------
DROP TABLE IF EXISTS `ai_providers`;
CREATE TABLE `ai_providers`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商编码（如 anthropic）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `provider_type` enum('OPENAI','ANTHROPIC','GOOGLE','AZURE_OPENAI','AWS_BEDROCK','DEEPSEEK','OPENAI_COMPATIBLE','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API Base URL',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 供应商表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_providers
-- ----------------------------
INSERT INTO `ai_providers` VALUES (1, 'openai', 'OpenAI', 'OPENAI', 'https://api.openai.com/v1', 'ACTIVE', '2025-01-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_providers` VALUES (2, 'anthropic', 'Anthropic', 'ANTHROPIC', 'https://api.anthropic.com', 'ACTIVE', '2025-01-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_providers` VALUES (3, 'deepseek', 'DeepSeek', 'DEEPSEEK', 'https://api.deepseek.com/v1', 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_providers` VALUES (4, 'siliconflow', 'SiliconFlow', 'OPENAI_COMPATIBLE', 'https://api.siliconflow.cn/v1', 'ACTIVE', '2025-04-10 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for ai_usage_daily
-- ----------------------------
DROP TABLE IF EXISTS `ai_usage_daily`;
CREATE TABLE `ai_usage_daily`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL COMMENT '统计日期',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '用户 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID',
  `provider_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '供应商 ID',
  `model_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '模型 ID',
  `client_app_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '客户端 ID',
  `total_requests` int UNSIGNED NOT NULL DEFAULT 0,
  `success_requests` int UNSIGNED NOT NULL DEFAULT 0,
  `blocked_requests` int UNSIGNED NOT NULL DEFAULT 0,
  `input_tokens` bigint UNSIGNED NOT NULL DEFAULT 0,
  `output_tokens` bigint UNSIGNED NOT NULL DEFAULT 0,
  `total_tokens` bigint UNSIGNED NOT NULL DEFAULT 0,
  `cost_amount` decimal(16, 6) NOT NULL DEFAULT 0.000000,
  `skill_invocations` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '技能调用次数',
  `tool_invocations` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '工具调用次数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dimension`(`stat_date` ASC, `user_id` ASC, `project_id` ASC, `provider_id` ASC, `model_id` ASC, `client_app_id` ASC) USING BTREE,
  INDEX `idx_date`(`stat_date` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 日用量聚合表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_usage_daily
-- ----------------------------
INSERT INTO `ai_usage_daily` VALUES (1, '2026-03-20', NULL, NULL, NULL, NULL, NULL, 15880, 15620, 42, 420000000, 98000000, 518000000, 18240.500000, 12000, 8900, '2026-03-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_usage_daily` VALUES (2, '2026-03-20', 5, 1, 2, 3, 1, 42, 41, 0, 168000, 39000, 207000, 0.980000, 28, 6, '2026-03-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_usage_daily` VALUES (3, '2026-03-20', 11, 1, 2, 3, 1, 38, 38, 0, 210000, 48000, 258000, 1.120000, 30, 4, '2026-03-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `ai_usage_daily` VALUES (4, '2026-03-19', NULL, 1, NULL, NULL, NULL, 920, 905, 3, 5120000, 1200000, 6320000, 28.400000, 210, 88, '2026-03-19 23:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for ai_usage_events
-- ----------------------------
DROP TABLE IF EXISTS `ai_usage_events`;
CREATE TABLE `ai_usage_events`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `credential_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '使用的平台凭证 ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '用户 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID',
  `provider_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT 'AI 供应商 ID',
  `provider_api_key_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '使用的上游 Key ID',
  `model_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '模型 ID',
  `client_app_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '客户端 ID',
  `source_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PLATFORM_GATEWAY' COMMENT '用量来源，与 AiUsageEvent.sourceType 对齐',
  `request_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CHAT' COMMENT '请求模式，与 AiUsageEvent.requestMode 对齐',
  `request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求追踪 ID',
  `conversation_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话 ID',
  `skill_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '触发的技能 ID',
  `input_tokens` bigint UNSIGNED NOT NULL DEFAULT 0,
  `output_tokens` bigint UNSIGNED NOT NULL DEFAULT 0,
  `total_tokens` bigint UNSIGNED NOT NULL DEFAULT 0,
  `cost_amount` decimal(16, 6) NOT NULL DEFAULT 0.000000 COMMENT '费用（USD）',
  `quota_check_result` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '双池配额检查结果',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAILED/BLOCKED_BY_QUOTA/BLOCKED_BY_POLICY 等',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `latency_ms` int UNSIGNED NULL DEFAULT NULL COMMENT '代理延迟（毫秒）',
  `occurred_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_credential`(`credential_id` ASC) USING BTREE,
  INDEX `idx_model`(`model_id` ASC) USING BTREE,
  INDEX `idx_skill`(`skill_id` ASC) USING BTREE,
  INDEX `idx_occurred`(`occurred_at` ASC) USING BTREE,
  INDEX `idx_request_id`(`request_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 用量明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_usage_events
-- ----------------------------
INSERT INTO `ai_usage_events` VALUES (1, 1, 5, 1, 2, 1, 3, 1, 'PLATFORM_GATEWAY', 'CHAT', 'req_7a2f9c1e', 'conv_01JQ', 1, 4200, 980, 5180, 0.024600, NULL, 'SUCCESS', NULL, 842, '2026-03-20 09:12:33', '2026-03-20 09:12:33');
INSERT INTO `ai_usage_events` VALUES (2, 2, 11, 1, 2, 1, 3, 1, 'PLATFORM_GATEWAY', 'CHAT', 'req_8b3e0d2f', 'conv_01JR', 1, 8900, 2100, 11000, 0.058000, NULL, 'SUCCESS', NULL, 1204, '2026-03-20 09:45:10', '2026-03-20 09:45:10');
INSERT INTO `ai_usage_events` VALUES (3, NULL, 7, 2, 3, 4, 5, NULL, 'PLATFORM_GATEWAY', 'CHAT', 'req_9c4f1e3a', NULL, 2, 12000, 3400, 15400, 0.006800, NULL, 'SUCCESS', NULL, 620, '2026-03-20 10:01:02', '2026-03-20 10:01:02');
INSERT INTO `ai_usage_events` VALUES (4, 3, 3, 3, 1, 3, 1, 3, 'PLATFORM_GATEWAY', 'CODE', 'req_ad5g2f4b', 'conv_ci_99', 3, 28000, 4200, 32200, 0.089000, NULL, 'SUCCESS', NULL, 2100, '2026-03-20 07:18:44', '2026-03-20 07:18:44');
INSERT INTO `ai_usage_events` VALUES (5, 1, 5, 1, 2, 1, 4, 1, 'PLATFORM_GATEWAY', 'CHAT', 'req_be6h3g5c', 'conv_01JS', NULL, 1200, 256, 1456, 0.002100, NULL, 'FAILED', 'upstream timeout after 4s', 4500, '2026-03-19 22:40:11', '2026-03-19 22:40:11');

-- ----------------------------
-- Table structure for alert_events
-- ----------------------------
DROP TABLE IF EXISTS `alert_events`;
CREATE TABLE `alert_events`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_id` bigint UNSIGNED NOT NULL COMMENT '告警规则 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联项目 ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联用户 ID',
  `trigger_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发时的实际值',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警消息内容',
  `notified_channels` json NULL COMMENT '已通知的渠道',
  `severity` enum('CRITICAL','HIGH','MEDIUM','LOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MEDIUM' COMMENT '告警级别（可与规则一致；历史数据建议回填）',
  `status` enum('FIRING','ACKNOWLEDGED','RESOLVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'FIRING',
  `resolved_at` datetime NULL DEFAULT NULL COMMENT '解决时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rule`(`rule_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警事件记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of alert_events
-- ----------------------------
INSERT INTO `alert_events` VALUES (1, 1, 1, NULL, '842.3', '项目 Omni-CS 昨日推断成本 842.3 USD，超过阈值', '[1]', 'HIGH', 'ACKNOWLEDGED', NULL, '2026-03-16 08:12:00');
INSERT INTO `alert_events` VALUES (2, 2, NULL, NULL, '0.061', 'Anthropic 主 Key 5 分钟错误率 6.1%', '[1]', 'CRITICAL', 'FIRING', NULL, '2026-03-20 09:40:00');

-- ----------------------------
-- Table structure for alert_rules
-- ----------------------------
DROP TABLE IF EXISTS `alert_rules`;
CREATE TABLE `alert_rules`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警描述',
  `trigger_condition` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发条件描述',
  `trigger_expression` json NOT NULL COMMENT '触发条件表达式（JSON 规则）',
  `severity` enum('CRITICAL','HIGH','MEDIUM','LOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MEDIUM' COMMENT '告警级别',
  `notification_channel_ids` json NULL COMMENT '通知渠道 ID 列表',
  `cooldown_minutes` int UNSIGNED NOT NULL DEFAULT 60 COMMENT '冷却时间（分钟）',
  `scope` enum('PLATFORM','PROJECT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PLATFORM' COMMENT '告警范围',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID（平台级为 NULL）',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `last_triggered_at` datetime NULL DEFAULT NULL COMMENT '最后触发时间',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_scope`(`scope` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of alert_rules
-- ----------------------------
INSERT INTO `alert_rules` VALUES (1, '项目日成本超阈值', '单项目估算日成本 > 800 USD', 'daily_estimated_cost_usd > 800', '{\"op\": \">\", \"value\": 800, \"metric\": \"daily_estimated_cost_usd\"}', 'HIGH', '[1, 2]', 120, 'PLATFORM', NULL, 'ACTIVE', '2026-03-15 08:10:00', 2, '2025-06-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `alert_rules` VALUES (2, '上游 Key 错误率', '5 分钟内错误率 > 5%', 'error_rate_5m > 0.05', '{\"op\": \">\", \"value\": 0.05, \"metric\": \"error_rate_5m\"}', 'CRITICAL', '[1]', 30, 'PLATFORM', NULL, 'ACTIVE', NULL, 1, '2025-06-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for atomic_capabilities
-- ----------------------------
DROP TABLE IF EXISTS `atomic_capabilities`;
CREATE TABLE `atomic_capabilities`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '能力名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '能力编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '能力描述',
  `icon` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `category` enum('MODEL_STANDARD','KNOWLEDGE_RETRIEVAL','AGENT','MCP_STANDARD','DATA_PROCESSING','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '能力分类',
  `doc_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '接入文档（Markdown）',
  `api_spec_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'OpenAPI 规范地址',
  `git_repo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Git 仓库地址',
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本号',
  `supported_languages` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的编程语言',
  `subscription_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '订阅项目数',
  `status` enum('ACTIVE','DEPRECATED','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业级原子能力库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of atomic_capabilities
-- ----------------------------
INSERT INTO `atomic_capabilities` VALUES (1, '平台统一聊天补全', 'cap.chat.complete', 'OpenAI 兼容 Chat Completions', '💬', 'MODEL_STANDARD', '# 接入说明\n使用平台凭证调用 /v1/chat/completions', 'https://api.cqcdi.tech/openapi/gateway.json', NULL, '2026.1', 'any', 3, 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `atomic_capabilities` VALUES (2, '向量检索服务', 'cap.vector.search', 'Milvus 托管检索', '🔍', 'KNOWLEDGE_RETRIEVAL', '# 向量检索\nPOST /v1/vector/search', 'https://api.cqcdi.tech/openapi/vector.json', NULL, '1.4.0', 'Python,Java', 2, 'ACTIVE', '2025-02-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for audit_logs
-- ----------------------------
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '操作用户 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联项目 ID',
  `action` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作动作',
  `target_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标对象类型',
  `target_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '目标对象 ID',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作详情（JSON）',
  `result` enum('SUCCESS','FILTERED','BLOCKED','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUCCESS',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作 IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端 User-Agent',
  `occurred_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_action`(`action` ASC) USING BTREE,
  INDEX `idx_occurred`(`occurred_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作审计日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of audit_logs
-- ----------------------------
INSERT INTO `audit_logs` VALUES (1, 2, NULL, 'provider_api_key.view', 'provider_api_key', 1, '{\"label\": \"Anthropic 生产主 Key\"}', 'SUCCESS', '10.10.1.2', 'Mozilla/5.0', '2026-03-20 08:55:00', '2026-03-20 23:45:00');
INSERT INTO `audit_logs` VALUES (2, 4, 1, 'skill.publish', 'skill', 1, '{\"version\": \"1.2.0\"}', 'SUCCESS', '10.12.9.20', 'Mozilla/5.0', '2026-03-19 17:20:00', '2026-03-20 23:45:00');
INSERT INTO `audit_logs` VALUES (3, 12, NULL, 'security.scan', 'platform', NULL, '{\"finding\": \"no_critical\"}', 'SUCCESS', '10.50.1.3', 'curl/8.5', '2026-03-18 06:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for client_apps
-- ----------------------------
DROP TABLE IF EXISTS `client_apps`;
CREATE TABLE `client_apps`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端编码（claude_code/cursor/codex）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端名称',
  `icon` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `supports_mcp` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否支持 MCP',
  `supports_custom_gateway` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否支持自定义网关',
  `setup_instruction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '接入指南模板（Markdown）',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支持接入的研发客户端表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of client_apps
-- ----------------------------
INSERT INTO `client_apps` VALUES (1, 'cursor', 'Cursor', '🖱️', 1, 1, '在 Settings → Models 中填入平台网关地址与平台凭证。', 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `client_apps` VALUES (2, 'vscode_github_copilot', 'VS Code + Copilot', '💻', 0, 1, '通过 HTTP 代理将请求转发至企业网关。', 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `client_apps` VALUES (3, 'claude_code', 'Claude Code', '🤖', 1, 1, '配置 ANTHROPIC_BASE_URL 指向平台 Anthropic 兼容端点。', 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门编码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门描述',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of departments
-- ----------------------------
INSERT INTO `departments` VALUES (1, '技术研发中心', 'RND', '产品线研发与架构', 'ACTIVE', '2024-05-06 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `departments` VALUES (2, '产品与体验部', 'PD', '产品规划与 UX', 'ACTIVE', '2024-05-06 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `departments` VALUES (3, '数据与智能部', 'DATA_AI', '数据平台与 AI 应用', 'ACTIVE', '2024-05-06 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `departments` VALUES (4, '质量与效能部', 'QE', '测试、SRE、研发效能', 'ACTIVE', '2024-05-06 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `departments` VALUES (5, '企业信息化部', 'IT', '内部系统与集成', 'ACTIVE', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `departments` VALUES (6, '安全合规部', 'SEC', '安全策略与审计', 'ACTIVE', '2024-07-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for deployments
-- ----------------------------
DROP TABLE IF EXISTS `deployments`;
CREATE TABLE `deployments`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `environment_id` bigint UNSIGNED NOT NULL COMMENT '环境 ID',
  `service_id` bigint UNSIGNED NOT NULL COMMENT '服务 ID',
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `pipeline_run_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '流水线运行 ID',
  `version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部署版本',
  `commit_sha` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Commit SHA',
  `deploy_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '部署者 ID',
  `change_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更描述',
  `duration_seconds` int UNSIGNED NULL DEFAULT NULL COMMENT '部署耗时（秒）',
  `status` enum('PENDING','DEPLOYING','SUCCESS','FAILED','ROLLED_BACK') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING',
  `deployed_at` datetime NULL DEFAULT NULL COMMENT '部署时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_environment`(`environment_id` ASC) USING BTREE,
  INDEX `idx_service`(`service_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_pipeline_run`(`pipeline_run_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部署记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deployments
-- ----------------------------
INSERT INTO `deployments` VALUES (1, 1, 1, 1, 1, '0.8.4-SNAPSHOT', 'a1b2c3d4', 5, 'OCR 路由灰度开关', 240, 'SUCCESS', '2026-03-19 18:05:00', '2026-03-19 18:05:00', '2026-03-20 23:45:00');
INSERT INTO `deployments` VALUES (2, 2, 1, 1, NULL, '0.8.3', 'e5f6a7b8', 10, '上周生产发布', 360, 'SUCCESS', '2026-03-12 22:35:00', '2026-03-12 22:35:00', '2026-03-20 23:45:00');
INSERT INTO `deployments` VALUES (3, 3, 7, 3, NULL, '0.9.2-rc1', '9c0d1e2f', 10, '预发：租户成本标签', 420, 'SUCCESS', '2026-03-17 20:12:00', '2026-03-17 20:12:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for dora_metrics
-- ----------------------------
DROP TABLE IF EXISTS `dora_metrics`;
CREATE TABLE `dora_metrics`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID（NULL=平台级）',
  `service_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '服务 ID',
  `metric_period` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '统计周期（如 2026-W12 / 2026-03）',
  `deploy_frequency` decimal(8, 2) NULL DEFAULT NULL COMMENT '部署频率（次/周）',
  `lead_time_hours` decimal(8, 2) NULL DEFAULT NULL COMMENT '变更前置时间（小时）',
  `mttr_hours` decimal(8, 2) NULL DEFAULT NULL COMMENT '故障恢复时间（小时）',
  `change_failure_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '变更失败率（%）',
  `ai_code_ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT 'AI 生成代码占比（%）',
  `ai_review_coverage` decimal(5, 2) NULL DEFAULT NULL COMMENT 'AI 代码审查覆盖率（%）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_service`(`service_id` ASC) USING BTREE,
  INDEX `idx_period`(`metric_period` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '研发效能指标表（DORA + AI 指标）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dora_metrics
-- ----------------------------
INSERT INTO `dora_metrics` VALUES (1, 1, 1, '2026-W11', 4.20, 18.50, 3.20, 8.50, 22.00, 55.00, '2026-03-17 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `dora_metrics` VALUES (2, 3, 7, '2026-W11', 6.80, 9.20, 1.80, 4.20, 38.00, 72.00, '2026-03-17 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `dora_metrics` VALUES (3, NULL, NULL, '2026-03', 5.10, 14.00, 2.60, 6.10, 28.50, 61.00, '2026-03-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for environments
-- ----------------------------
DROP TABLE IF EXISTS `environments`;
CREATE TABLE `environments`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `service_id` bigint UNSIGNED NOT NULL COMMENT '服务 ID',
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '环境名称',
  `env_type` enum('DEV','STAGING','PROD','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境 URL',
  `current_branch` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前分支',
  `current_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前版本',
  `current_commit` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前 Commit SHA',
  `deploy_strategy` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部署策略',
  `instance_count` int UNSIGNED NOT NULL DEFAULT 1,
  `health_status` enum('HEALTHY','WARNING','UNHEALTHY','UNKNOWN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNKNOWN',
  `last_deploy_at` datetime NULL DEFAULT NULL COMMENT '最后部署时间',
  `last_deploy_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '最后部署者',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_service`(`service_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务运行环境表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of environments
-- ----------------------------
INSERT INTO `environments` VALUES (1, 1, 1, 'cs-conversation-api DEV', 'DEV', 'http://cs-conv.dev.internal:8080', 'feature/ocr-routing', '0.8.4-SNAPSHOT', 'a1b2c3d4', 'Rolling', 2, 'HEALTHY', '2026-03-19 18:00:00', 5, 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `environments` VALUES (2, 1, 1, 'cs-conversation-api PROD', 'PROD', 'https://cs-conv.cqcdi.tech', 'release/2026.3.1', '0.8.3', 'e5f6a7b8', 'BlueGreen', 6, 'HEALTHY', '2026-03-12 22:30:00', 10, 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `environments` VALUES (3, 7, 3, 'dev-copilot-gateway STAGING', 'STAGING', 'https://devgw-stg.cqcdi.tech', 'release/0.9.x', '0.9.2-rc1', '9c0d1e2f', 'Canary', 3, 'WARNING', '2026-03-17 20:10:00', 10, 'ACTIVE', '2025-07-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for epics
-- ----------------------------
DROP TABLE IF EXISTS `epics`;
CREATE TABLE `epics`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Epic 名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Epic 描述',
  `status` enum('OPEN','IN_PROGRESS','DONE','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OPEN',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Epic（史诗）表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of epics
-- ----------------------------
INSERT INTO `epics` VALUES (1, 1, '多模态进线', '图片工单、语音摘要统一接入', 'IN_PROGRESS', '2025-08-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `epics` VALUES (2, 2, '预测性补货', '基于销量与天气的补货建议', 'OPEN', '2025-09-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `epics` VALUES (3, 3, '网关可观测性', 'Trace、按租户成本拆解', 'IN_PROGRESS', '2025-10-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for incidents
-- ----------------------------
DROP TABLE IF EXISTS `incidents`;
CREATE TABLE `incidents`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `service_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联服务 ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事故标题',
  `severity` enum('CRITICAL','WARNING','INFO') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('PENDING','IN_PROGRESS','RESOLVED','CLOSED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING',
  `error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误堆栈',
  `error_request` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误请求信息',
  `ai_diagnosis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI 诊断结果',
  `ai_diagnosis_status` enum('NONE','ANALYZING','COMPLETED','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NONE',
  `assignee_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '负责人 ID',
  `github_issue_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'GitHub Issue 链接',
  `resolved_at` datetime NULL DEFAULT NULL COMMENT '解决时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_service`(`service_id` ASC) USING BTREE,
  INDEX `idx_severity`(`severity` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '事故/告警表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of incidents
-- ----------------------------
INSERT INTO `incidents` VALUES (1, 1, 1, '生产会话 API 偶发 502', 'WARNING', 'RESOLVED', 'upstream reset by peer', 'POST /v1/sessions/merge', '疑似连接池耗尽，建议调大 maxPool 并开启 keep-alive', 'COMPLETED', 5, 'https://git.cqcdi.tech/omni/cs-conversation-api/-/issues/412', '2026-03-14 11:30:00', '2026-03-14 09:10:00', '2026-03-20 23:45:00');
INSERT INTO `incidents` VALUES (2, 3, 7, '网关 STAGING P95 > 3s', 'CRITICAL', 'IN_PROGRESS', NULL, 'POST /v1/chat/completions', NULL, 'ANALYZING', 3, NULL, NULL, '2026-03-20 09:30:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for kb_documents
-- ----------------------------
DROP TABLE IF EXISTS `kb_documents`;
CREATE TABLE `kb_documents`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `kb_id` bigint UNSIGNED NOT NULL COMMENT '所属知识库 ID',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文档标题',
  `file_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件类型（pdf/docx/md/txt/code）',
  `file_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储路径',
  `file_size` bigint UNSIGNED NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `chunk_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '分块数量',
  `hit_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '被检索命中次数',
  `inject_mode` enum('AUTO_INJECT','ON_DEMAND') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ON_DEMAND' COMMENT '注入方式',
  `ref_projects` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '被引用的项目数',
  `status` enum('PENDING','PROCESSING','READY','ERROR') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理错误信息',
  `uploaded_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '上传者 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb`(`kb_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库文档表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of kb_documents
-- ----------------------------
INSERT INTO `kb_documents` VALUES (1, 1, '远程办公 VPN 排障手册 v3.2', 'pdf', 's3://kb-global/it/vpn-troubleshoot-v3.2.pdf', 2400000, 48, 1204, 'ON_DEMAND', 5, 'READY', NULL, 10, '2025-01-12 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `kb_documents` VALUES (2, 1, 'AD 域账号解锁流程', 'md', 's3://kb-global/it/ad-unlock.md', 12800, 6, 332, 'ON_DEMAND', 4, 'READY', NULL, 10, '2025-01-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `kb_documents` VALUES (3, 2, '客户数据最小化原则（对外）', 'docx', 's3://kb-global/legal/data-minimization.docx', 890000, 22, 890, 'AUTO_INJECT', 3, 'READY', NULL, 12, '2025-02-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `kb_documents` VALUES (4, 3, '渠道路由表-2026Q1', 'xlsx', 's3://kb-proj-omni/routing/channel-routing-2026q1.xlsx', 456000, 18, 210, 'ON_DEMAND', 1, 'READY', NULL, 4, '2025-03-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `kb_documents` VALUES (5, 3, 'RAG 评测 badcase 汇总', 'md', 's3://kb-proj-omni/qa/rag-badcases.md', 67000, 14, 56, 'ON_DEMAND', 1, 'PROCESSING', NULL, 11, '2026-03-18 15:00:00', '2026-03-20 23:45:00');
INSERT INTO `kb_documents` VALUES (7, 4, '开发规范', 'md', '2026/03/23/开发规范_1774269769544.md', 21287, 14, 0, 'ON_DEMAND', 0, 'READY', NULL, NULL, '2026-03-23 20:42:52', '2026-03-23 21:00:07');

-- ----------------------------
-- Table structure for key_rotation_logs
-- ----------------------------
DROP TABLE IF EXISTS `key_rotation_logs`;
CREATE TABLE `key_rotation_logs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `target_type` enum('PLATFORM_CREDENTIAL','PROVIDER_API_KEY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮换目标类型',
  `target_id` bigint UNSIGNED NOT NULL COMMENT '目标 ID',
  `target_label` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标标识（如供应商名 / 用户名）',
  `rotation_type` enum('MANUAL','SCHEDULED','EMERGENCY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MANUAL' COMMENT '轮换方式',
  `old_key_prefix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '旧 Key 前缀',
  `new_key_prefix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '新 Key 前缀',
  `result` enum('SUCCESS','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮换结果',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `operated_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '操作人 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥轮换日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of key_rotation_logs
-- ----------------------------
INSERT INTO `key_rotation_logs` VALUES (1, 'PROVIDER_API_KEY', 1, 'Anthropic 生产主 Key', 'SCHEDULED', 'sk-ant-…old', 'sk-ant-api03-…9f2a', 'SUCCESS', NULL, 2, '2026-02-01 03:00:00');
INSERT INTO `key_rotation_logs` VALUES (2, 'PLATFORM_CREDENTIAL', 2, '谢琳-Mac Studio', 'MANUAL', 'plt_u11_…', 'plt_u11_c3d4', 'SUCCESS', NULL, 11, '2026-01-15 16:20:00');

-- ----------------------------
-- Table structure for knowledge_bases
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_bases`;
CREATE TABLE `knowledge_bases`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识库名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '知识库描述',
  `scope` enum('GLOBAL','PROJECT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作用域：全局/项目级',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属项目 ID（全局为 NULL）',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类（技术规范/产品文档/安全合规等）',
  `embedding_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'bge-m3' COMMENT '向量化模型',
  `doc_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '文档数量',
  `total_chunks` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '总知识块数',
  `hit_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '知识库命中率（%）',
  `inject_mode` enum('AUTO_INJECT','ON_DEMAND','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ON_DEMAND' COMMENT '注入模式',
  `status` enum('ACTIVE','INACTIVE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '与 KnowledgeBase 实体一致',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scope`(`scope` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库表（全局知识库 + 项目知识库容器）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_bases
-- ----------------------------
INSERT INTO `knowledge_bases` VALUES (1, '企业 IT 服务台知识库', 'VPN、邮箱、账号权限等常见问题', 'GLOBAL', NULL, '需求文档', 'bge-m3', 128, 6420, 78.50, 'ON_DEMAND', 'ACTIVE', 10, '2025-01-10 10:00:00', '2026-03-23 16:43:06');
INSERT INTO `knowledge_bases` VALUES (2, '客服话术与合规', '禁用语、升级路径、隐私话术', 'GLOBAL', NULL, '需求文档', 'bge-m3', 56, 2104, 82.30, 'AUTO_INJECT', 'ACTIVE', 12, '2025-02-01 10:00:00', '2026-03-23 16:43:12');
INSERT INTO `knowledge_bases` VALUES (3, 'Omni-CS 产品私有库', '本项目工单字段、路由规则、租户差异', 'PROJECT', 1, '需求文档', 'bge-m3', 34, 980, 71.20, 'ON_DEMAND', 'ACTIVE', 4, '2025-03-01 10:00:00', '2026-03-23 16:43:09');
INSERT INTO `knowledge_bases` VALUES (4, '研发规范', NULL, 'GLOBAL', NULL, '技术规范', 'bge-m3', 0, 0, NULL, 'ON_DEMAND', 'ACTIVE', NULL, '2026-03-23 20:37:38', '2026-03-23 20:37:38');

-- ----------------------------
-- Table structure for knowledge_search_logs
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_search_logs`;
CREATE TABLE `knowledge_search_logs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `kb_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '知识库 ID（NULL=跨库检索）',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '用户 ID',
  `query` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '检索 Query',
  `search_scope` enum('GLOBAL','PROJECT','ALL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ALL' COMMENT '检索范围',
  `result_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '返回结果数',
  `hit_doc_ids` json NULL COMMENT '命中的文档 ID 列表',
  `relevance_score` decimal(5, 4) NULL DEFAULT NULL COMMENT '最佳结果相关度分数',
  `latency_ms` int UNSIGNED NULL DEFAULT NULL COMMENT '检索耗时（毫秒）',
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检索来源',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb`(`kb_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库检索日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_search_logs
-- ----------------------------
INSERT INTO `knowledge_search_logs` VALUES (3, 4, NULL, NULL, '开发规范', 'GLOBAL', 0, NULL, NULL, 266, 'KB_CONSOLE_TEST', '2026-03-23 21:15:39');

-- ----------------------------
-- Table structure for mcp_servers
-- ----------------------------
DROP TABLE IF EXISTS `mcp_servers`;
CREATE TABLE `mcp_servers`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `server_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务器唯一名称',
  `display_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '展示名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务描述',
  `server_type` enum('BUILTIN','OFFICIAL','ENTERPRISE','PROJECT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属项目 ID',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `server_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MCP 服务器地址',
  `auth_type` enum('NONE','BEARER','OAUTH2','API_KEY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NONE' COMMENT '认证方式',
  `auth_config` json NULL COMMENT '认证配置（加密存储）',
  `capabilities` json NULL COMMENT '已发现的能力',
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标 URL',
  `doc_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档地址',
  `status` enum('ACTIVE','DISABLED','ERROR') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `last_checked_at` datetime NULL DEFAULT NULL COMMENT '最后连通性检查时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_server_name`(`server_name` ASC) USING BTREE,
  INDEX `idx_server_type`(`server_type` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'MCP 服务器注册表（集成市场）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mcp_servers
-- ----------------------------
INSERT INTO `mcp_servers` VALUES (1, 'gitlab-mcp', 'GitLab MCP', 'MR、Issue、Pipeline 只读查询', 'ENTERPRISE', NULL, 'code', 'https://mcp.cqcdi.tech/gitlab/sse', 'BEARER', NULL, '{\"tools\": [\"list_merge_requests\", \"get_pipeline\"]}', NULL, NULL, 'ACTIVE', '2026-03-20 08:30:00', '2025-05-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `mcp_servers` VALUES (2, 'jira-mcp', 'Jira MCP', '工单搜索与评论追加', 'ENTERPRISE', NULL, 'project_mgmt', 'https://mcp.cqcdi.tech/jira/sse', 'OAUTH2', NULL, '{\"tools\": [\"search_issues\", \"add_comment\"]}', NULL, NULL, 'ACTIVE', '2026-03-19 22:00:00', '2025-05-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `mcp_servers` VALUES (3, 'local-docs-mcp', 'Omni 本地文档 MCP', '项目内 Markdown 资源', 'PROJECT', 1, 'documentation', 'http://127.0.0.1:8811/sse', 'NONE', NULL, '{\"resources\": [\"file://docs/**\"]}', NULL, NULL, 'ACTIVE', '2026-03-18 19:00:00', '2025-06-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for member_ai_quotas
-- ----------------------------
DROP TABLE IF EXISTS `member_ai_quotas`;
CREATE TABLE `member_ai_quotas`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID（NULL=个人总配额）',
  `quota_type` enum('TOKEN_QUOTA','COST_QUOTA','REQUEST_QUOTA') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配额类型',
  `quota_limit` bigint UNSIGNED NOT NULL COMMENT '配额上限',
  `used_amount` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '已用量',
  `reset_cycle` enum('DAILY','WEEKLY','MONTHLY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MONTHLY' COMMENT '重置周期',
  `last_reset_at` datetime NULL DEFAULT NULL COMMENT '上次重置时间',
  `status` enum('ACTIVE','INACTIVE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '与 MemberAiQuota 实体一致',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_project_type`(`user_id` ASC, `project_id` ASC, `quota_type` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成员 AI 配额表（个人总配额 + 项目级配额）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of member_ai_quotas
-- ----------------------------
INSERT INTO `member_ai_quotas` VALUES (1, 5, NULL, 'TOKEN_QUOTA', 5000000, 1284000, 'MONTHLY', '2026-03-01 00:00:00', 'ACTIVE', '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `member_ai_quotas` VALUES (2, 11, 1, 'TOKEN_QUOTA', 2000000, 1750000, 'MONTHLY', '2026-03-01 00:00:00', 'INACTIVE', '2025-09-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `member_ai_quotas` VALUES (3, 5, 1, 'COST_QUOTA', 50000, 32210, 'MONTHLY', '2026-03-01 00:00:00', 'ACTIVE', '2025-09-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `member_ai_quotas` VALUES (4, 6, 3, 'REQUEST_QUOTA', 50000, 12880, 'MONTHLY', '2026-03-01 00:00:00', 'ACTIVE', '2025-10-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for notification_channels
-- ----------------------------
DROP TABLE IF EXISTS `notification_channels`;
CREATE TABLE `notification_channels`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道名称',
  `channel_type` enum('EMAIL','WECHAT_WORK','DINGTALK','SLACK','WEBHOOK','SMS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道类型',
  `config` json NOT NULL COMMENT '渠道配置（Webhook URL / 群 ID 等）',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认渠道',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`channel_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知渠道表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification_channels
-- ----------------------------
INSERT INTO `notification_channels` VALUES (1, '研发效能-企业微信群', 'WECHAT_WORK', '{\"webhook\": \"https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=DUMMY\"}', 1, 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `notification_channels` VALUES (2, '平台告警邮件组', 'EMAIL', '{\"to\": [\"sre@cqcdi.tech\"], \"smtp\": \"smtp.cqcdi.tech\"}', 0, 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for oauth_connections
-- ----------------------------
DROP TABLE IF EXISTS `oauth_connections`;
CREATE TABLE `oauth_connections`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `mcp_server_id` bigint UNSIGNED NOT NULL COMMENT 'MCP 服务器 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID（NULL=平台级）',
  `provider_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OAuth 提供方（github/slack/gitlab）',
  `access_token_encrypted` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '加密的 Access Token',
  `refresh_token_encrypted` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '加密的 Refresh Token',
  `token_expires_at` datetime NULL DEFAULT NULL COMMENT 'Token 过期时间',
  `scopes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '授权范围',
  `account_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '授权账号名称',
  `status` enum('ACTIVE','EXPIRED','REVOKED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `connected_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '授权操作者 ID',
  `connected_at` datetime NULL DEFAULT NULL COMMENT '授权时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_mcp_server`(`mcp_server_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方 OAuth 连接表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oauth_connections
-- ----------------------------
INSERT INTO `oauth_connections` VALUES (1, 2, 1, 'atlassian', 'enc:atlassian_access_dummy', 'enc:atlassian_refresh_dummy', '2026-06-01 00:00:00', 'read:jira-work write:jira-work', 'svc-omni-jira', 'ACTIVE', 4, '2025-06-01 11:00:00', '2025-06-01 11:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for permission_definitions
-- ----------------------------
DROP TABLE IF EXISTS `permission_definitions`;
CREATE TABLE `permission_definitions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限模块（知识库/技能库/工具集/集成/成员管理/配额管理/项目设置）',
  `permission_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限点编码（如 knowledge.upload / skill.publish）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限点名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限点描述',
  `permission_scope` enum('PLATFORM','PROJECT','BOTH') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'BOTH' COMMENT '权限适用范围',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_key`(`permission_key` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限点定义表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of permission_definitions
-- ----------------------------
INSERT INTO `permission_definitions` VALUES (1, 'knowledge', 'knowledge.view', '查看知识库', '浏览知识库与文档', 'BOTH', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `permission_definitions` VALUES (2, 'knowledge', 'knowledge.upload', '上传文档', '上传与更新文档', 'PROJECT', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `permission_definitions` VALUES (3, 'skill', 'skill.publish', '发布技能', '发布与下线技能', 'PROJECT', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `permission_definitions` VALUES (4, 'tool', 'tool.invoke', '调用工具', '通过网关/MCP 调用工具', 'BOTH', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `permission_definitions` VALUES (5, 'quota', 'quota.manage', '管理配额', '调整成员与项目配额', 'PLATFORM', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `permission_definitions` VALUES (6, 'credential', 'credential.rotate', '轮换密钥', '上游 Key 与平台凭证轮换', 'PLATFORM', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `permission_definitions` VALUES (7, 'user', 'user.list', '查看用户列表', '查询平台所有用户', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (8, 'user', 'user.create', '新增用户', '创建平台账号', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (9, 'user', 'user.edit', '编辑用户', '修改用户信息', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (10, 'user', 'user.disable', '停用用户', '切换用户账号状态', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (11, 'user', 'user.invite', '邀请用户', '发送邀请邮件', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (12, 'department', 'dept.list', '查看部门', '查询部门列表', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (13, 'department', 'dept.manage', '管理部门', '新增/编辑/删除部门', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (14, 'project', 'project.list', '查看项目列表', '查询平台所有项目', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (15, 'project', 'project.create', '创建项目', '新建项目', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (16, 'project', 'project.edit', '编辑项目', '修改项目基本信息', 'BOTH', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (17, 'project', 'project.delete', '删除项目', '删除项目', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (18, 'project', 'project.member', '管理项目成员', '添加/移除项目成员', 'BOTH', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (19, 'model', 'model.list', '查看模型列表', '查询 AI 模型', 'BOTH', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (20, 'model', 'model.config', '配置模型', '新增/编辑 AI 模型', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (21, 'model', 'model.routing', '配置路由策略', '管理模型路由与故障转移', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (22, 'credential', 'credential.list', '查看凭证列表', '查询平台凭证', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (23, 'credential', 'credential.create', '创建凭证', '生成新凭证', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (24, 'credential', 'credential.revoke', '吊销凭证', '撤销有效凭证', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (25, 'audit', 'alert.view', '查看告警', '查看告警规则和事件', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (26, 'audit', 'alert.manage', '管理告警', '创建/编辑告警规则', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (27, 'audit', 'audit.view', '查看审计', '查询操作和活动日志', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (28, 'platform', 'platform.settings', '平台全局设置', '修改平台配置', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `permission_definitions` VALUES (29, 'platform', 'platform.rbac', '角色权限管理', '管理角色和权限', 'PLATFORM', '2026-03-23 10:44:11', '2026-03-23 10:44:11');

-- ----------------------------
-- Table structure for pipeline_runs
-- ----------------------------
DROP TABLE IF EXISTS `pipeline_runs`;
CREATE TABLE `pipeline_runs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `service_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '服务 ID',
  `trigger_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '触发者 ID',
  `trigger_type` enum('MANUAL','WEBHOOK','SCHEDULED','AI_TRIGGERED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `pipeline_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流水线名称',
  `branch` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分支',
  `commit_sha` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Commit SHA',
  `status` enum('PENDING','RUNNING','SUCCESS','FAILED','ABORTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `duration_seconds` int UNSIGNED NULL DEFAULT NULL COMMENT '总耗时（秒）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_service`(`service_id` ASC) USING BTREE,
  INDEX `idx_trigger_type`(`trigger_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'CI/CD 流水线运行记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pipeline_runs
-- ----------------------------
INSERT INTO `pipeline_runs` VALUES (1, 1, 1, 5, 'WEBHOOK', 'cs-conversation-api-ci', 'feature/ocr-routing', 'a1b2c3d4', 'SUCCESS', '2026-03-19 17:50:00', '2026-03-19 18:05:00', 900, '2026-03-19 17:50:00', '2026-03-20 23:45:00');
INSERT INTO `pipeline_runs` VALUES (2, 3, 7, 3, 'MANUAL', 'dev-copilot-gateway-ci', 'release/0.9.x', '9c0d1e2f', 'RUNNING', '2026-03-20 10:02:00', NULL, NULL, '2026-03-20 10:02:00', '2026-03-20 23:45:00');
INSERT INTO `pipeline_runs` VALUES (3, 2, 4, 7, 'WEBHOOK', 'tower-metrics-api-ci', 'main', 'f0e1d2c3', 'FAILED', '2026-03-18 23:10:00', '2026-03-18 23:18:00', 480, '2026-03-18 23:10:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for pipeline_stages
-- ----------------------------
DROP TABLE IF EXISTS `pipeline_stages`;
CREATE TABLE `pipeline_stages`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `pipeline_run_id` bigint UNSIGNED NOT NULL COMMENT '流水线运行 ID',
  `stage_order` int UNSIGNED NOT NULL COMMENT '阶段顺序',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '阶段名称',
  `status` enum('PENDING','RUNNING','SUCCESS','FAILED','SKIPPED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pipeline_run`(`pipeline_run_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'CI/CD 流水线阶段表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pipeline_stages
-- ----------------------------
INSERT INTO `pipeline_stages` VALUES (1, 1, 1, '编译与单测', 'SUCCESS', '2026-03-19 17:50:00', '2026-03-19 17:56:00', NULL, '2026-03-19 17:50:00', '2026-03-20 23:45:00');
INSERT INTO `pipeline_stages` VALUES (2, 1, 2, '构建镜像', 'SUCCESS', '2026-03-19 17:56:00', '2026-03-19 18:01:00', NULL, '2026-03-19 17:50:00', '2026-03-20 23:45:00');
INSERT INTO `pipeline_stages` VALUES (3, 1, 3, '部署 DEV', 'SUCCESS', '2026-03-19 18:01:00', '2026-03-19 18:05:00', NULL, '2026-03-19 17:50:00', '2026-03-20 23:45:00');
INSERT INTO `pipeline_stages` VALUES (4, 2, 1, '编译与单测', 'RUNNING', '2026-03-20 10:02:00', NULL, NULL, '2026-03-20 10:02:00', '2026-03-20 23:45:00');
INSERT INTO `pipeline_stages` VALUES (5, 3, 1, '集成测试', 'FAILED', '2026-03-18 23:12:00', '2026-03-18 23:17:00', 'OrderSnapshotTest 断言失败', '2026-03-18 23:10:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for platform_credential_projects
-- ----------------------------
DROP TABLE IF EXISTS `platform_credential_projects`;
CREATE TABLE `platform_credential_projects`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `credential_id` bigint UNSIGNED NOT NULL COMMENT '平台凭证 ID，关联 platform_credentials.id',
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID，关联 projects.id；与 credential_id 唯一成对',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定关系创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_platform_credential_projects_pair`(`credential_id` ASC, `project_id` ASC) USING BTREE,
  INDEX `idx_platform_credential_projects_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '平台凭证与项目授权关联：一条凭证可绑定多个项目，实现一证多项目' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of platform_credential_projects
-- ----------------------------

-- ----------------------------
-- Table structure for platform_credentials
-- ----------------------------
DROP TABLE IF EXISTS `platform_credentials`;
CREATE TABLE `platform_credentials`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NOT NULL COMMENT '所属用户 ID（一人一证，与实体 uk 一致）',
  `bound_project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '当前工作项目 ID（RAG/上下文）；NULL 表示未指定，由管理端写入、网关只读',
  `credential_type` enum('PERSONAL','SERVICE_ACCOUNT','TEMP') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PERSONAL' COMMENT '凭证类型，与 PlatformCredential 实体一致',
  `key_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SHA256(key)，不存明文',
  `key_prefix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'plt_xxx_ 前缀，用于展示',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '凭证展示名称',
  `monthly_token_quota` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '个人月度 Token 上限，0=不限制',
  `used_tokens_this_month` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '个人当月已消耗 Token',
  `alert_threshold_pct` tinyint UNSIGNED NOT NULL DEFAULT 80 COMMENT '个人池告警阈值百分比',
  `over_quota_strategy` enum('BLOCK','ALLOW_WITH_ALERT','DOWNGRADE_MODEL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'BLOCK' COMMENT '超配额策略',
  `last_quota_reset_at` datetime NULL DEFAULT NULL COMMENT '个人池配额最近一次重置时间',
  `status` enum('ACTIVE','DISABLED','REVOKED','EXPIRED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '与 PlatformCredential 实体一致',
  `expires_at` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `last_used_at` datetime NULL DEFAULT NULL COMMENT '最后使用时间',
  `last_used_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后使用 IP',
  `revoked_at` datetime NULL DEFAULT NULL COMMENT '吊销时间',
  `revoke_reason` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '吊销原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_key_hash`(`key_hash` ASC) USING BTREE,
  UNIQUE INDEX `uk_platform_credentials_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_bound_project`(`bound_project_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_expires`(`expires_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台凭证表（成员接入 AI 工具的统一凭证）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of platform_credentials
-- ----------------------------
INSERT INTO `platform_credentials` VALUES (1, 1, NULL, 'PERSONAL', '255535f97cd48c07288fdf0502c158b3ec00abd3f2a8e444abc6e7b20e70f94d', 'plt_1_000000', '唐浩-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (2, 2, NULL, 'PERSONAL', 'bfbab795b6c731b7cde15cdd5aa94a3877b2e6c91fe470c58392a63a77d1a42f', 'plt_2_000000', '刘梅-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (3, 3, NULL, 'PERSONAL', 'cca3220ba70ddfb668cbf757635bf3f997a79a42720a046b2aa7ec3022cebba1', 'plt_3_000000', '赵磊-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (4, 4, NULL, 'PERSONAL', '00d181e9781407cada1aa42d5511a1a88e5365f005a9b70f9322d9137c1e278c', 'plt_4_000000', '孙倩-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (5, 5, NULL, 'PERSONAL', '66ea0c4570370be9cef360436769cd6a7f316b0ed2b03430ac839beedb7e1456', 'plt_5_000000', '周浩-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (6, 6, NULL, 'PERSONAL', '7543d023dc052bbb5736bc0bff4f1625eca50b1bab47b9a0e43e059dfccd9a86', 'plt_6_000000', '吴婷-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (7, 7, NULL, 'PERSONAL', 'dcdd6c8f2926674de8be8c228a98d1ff2dd74007393adacf04572be532dca54d', 'plt_7_000000', '郑凯-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (8, 8, NULL, 'PERSONAL', '121fbfc68995d9efd17904fc4853bed1a566ad316bcb5d75b93d6ed9f70625ea', 'plt_8_000000', '冯楠-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (9, 9, NULL, 'PERSONAL', 'eecd6fe7dea9b07716e9a57045198549d15e7c637caf57a76e142345f8b39fb0', 'plt_9_000000', '何妍-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (10, 10, NULL, 'PERSONAL', '9ce6070d45874ba521b9aa797e9ccd41c315f30e33ff1f7b362255f9d7c07c61', 'plt_10_00000', '江默-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (11, 11, NULL, 'PERSONAL', '382c5ddba360c3621f2d2362120f51070092d5c75d4c495b7c230095a51cbd10', 'plt_11_00000', '谢琳-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `platform_credentials` VALUES (12, 12, NULL, 'PERSONAL', 'b5cddabd7bb290ad6b55850c82602abc9a1a3201a43d0db287379544a59a8ba0', 'plt_12_00000', '董泽-个人凭证（种子）', 0, 0, 80, 'BLOCK', NULL, 'ACTIVE', NULL, NULL, NULL, NULL, NULL, '2025-08-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for platform_roles
-- ----------------------------
DROP TABLE IF EXISTS `platform_roles`;
CREATE TABLE `platform_roles`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码，全局唯一，如 SUPER_ADMIN',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色显示名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `is_system` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否系统内置（不可删除）',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台角色定义表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of platform_roles
-- ----------------------------
INSERT INTO `platform_roles` VALUES (1, 'SUPER_ADMIN', '超级管理员', '全平台配置与用户管理，拥有所有权限', 1, 'ACTIVE', '2026-03-23 10:42:21', '2026-03-23 10:42:21');
INSERT INTO `platform_roles` VALUES (2, 'PLATFORM_ADMIN', '平台管理员', '模型、密钥、告警与审计管理', 1, 'ACTIVE', '2026-03-23 10:42:21', '2026-03-23 10:42:21');
INSERT INTO `platform_roles` VALUES (3, 'MEMBER', '普通成员', '日常 AI 能力使用，受配额限制', 1, 'ACTIVE', '2026-03-23 10:42:21', '2026-03-23 10:42:21');

-- ----------------------------
-- Table structure for platform_settings
-- ----------------------------
DROP TABLE IF EXISTS `platform_settings`;
CREATE TABLE `platform_settings`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设置键',
  `setting_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设置值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置说明',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_key`(`setting_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台全局设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of platform_settings
-- ----------------------------
INSERT INTO `platform_settings` VALUES (1, 'gateway.public_base_url', 'https://ai-gw.cqcdi.tech', 'OpenAI 兼容网关对外地址', '2026-01-10 10:00:00');
INSERT INTO `platform_settings` VALUES (2, 'billing.currency', 'USD', '成本核算币种', '2026-01-10 10:00:00');
INSERT INTO `platform_settings` VALUES (3, 'feature.mcp_beta', 'true', '是否对全员开放 MCP 集成市场', '2026-03-01 10:00:00');

-- ----------------------------
-- Table structure for project_agents
-- ----------------------------
DROP TABLE IF EXISTS `project_agents`;
CREATE TABLE `project_agents`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '所属项目ID，唯一',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '智能体名称，默认「{项目名}助手」',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '智能体描述',
  `avatar_icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '头像图标（emoji 或 icon name）',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '项目智能体全局 System Prompt',
  `preferred_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '优先使用的模型代码，如 gpt-4o',
  `enable_rag` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用知识库 RAG 增强',
  `enable_skills` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用项目技能注入',
  `enable_tools` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用工具调用',
  `enable_deploy` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用部署指令能力',
  `enable_monitoring` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用监控告警查询能力',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'ACTIVE',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_agents_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_project_agents_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '项目专属智能体配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_agents
-- ----------------------------
INSERT INTO `project_agents` VALUES (1, 5, '测试 · 乐知', '乐知是 测试 项目的专属 AI 助手，乐于求知、乐于解答。负责回答项目相关的所有问题，包括 Bug 排查、发布运维、文档查询等。', '🔮', '你是【测试】项目的专属 AI 助手「乐知」。\n\n## 项目基本信息\n- 项目名称：测试\n- 项目编码：CODE\n- 项目类型：PRODUCT\n- 项目描述：-\n\n## 你的职责\n你可以帮助项目成员解答以下问题：\n1. **Bug 排查**：分析项目日志、错误信息，给出诊断和修复建议\n2. **运行状态**：查询项目当前服务运行状态、告警信息\n3. **文档查询**：检索项目相关文档、规范、知识库内容\n4. **代码辅助**：基于项目技术栈提供代码建议和审查\n5. **发布管理**：了解当前部署状态，在授权后协助触发发布流程\n\n## 回答规范\n- 回答应简洁、准确，聚焦于项目实际情况\n- 对于不确定的信息，主动说明并建议查阅具体文档或联系相关负责人\n- 涉及生产发布、配置变更等高风险操作，必须先确认操作人身份和意图\n', NULL, 1, 1, 1, 0, 0, 'ACTIVE', NULL, '2026-03-22 22:06:36', '2026-03-22 22:06:36');

-- ----------------------------
-- Table structure for project_ai_policies
-- ----------------------------
DROP TABLE IF EXISTS `project_ai_policies`;
CREATE TABLE `project_ai_policies`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `policy_type` enum('QUOTA_LIMIT','COST_LIMIT','CAPABILITY_RESTRICT','PROVIDER_RESTRICT','RATE_LIMIT','SECURITY_POLICY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '策略类型',
  `rule_content` json NOT NULL COMMENT '策略规则内容（JSON）',
  `priority` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '优先级',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_type`(`policy_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目 AI 策略表（配额/限制/安全规则）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_ai_policies
-- ----------------------------
INSERT INTO `project_ai_policies` VALUES (1, 1, 'COST_LIMIT', '{\"monthly_usd_cap\": 12000, \"alert_at_percent\": 85}', 10, 'ACTIVE', '2025-02-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_ai_policies` VALUES (2, 1, 'PROVIDER_RESTRICT', '{\"allowed_provider_codes\": [\"anthropic\", \"deepseek\"]}', 20, 'ACTIVE', '2025-02-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_ai_policies` VALUES (3, 3, 'RATE_LIMIT', '{\"rpm_per_user\": 120, \"tpm_per_user\": 80000}', 5, 'ACTIVE', '2025-05-20 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for project_ai_quotas
-- ----------------------------
DROP TABLE IF EXISTS `project_ai_quotas`;
CREATE TABLE `project_ai_quotas`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID，关联 projects.id',
  `quota_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配额类型：TOKEN_QUOTA / COST_QUOTA / REQUEST_QUOTA 等',
  `quota_limit` bigint NOT NULL COMMENT '本周期内该项目全体共享上限',
  `used_amount` bigint NOT NULL DEFAULT 0 COMMENT '本周期内项目内所有成员已用汇总（由业务在记用量时累加）',
  `reset_cycle` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MONTHLY' COMMENT '重置周期：DAILY/WEEKLY/MONTHLY',
  `last_reset_at` datetime NULL DEFAULT NULL COMMENT '上次重置时间',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_ai_quotas_project_type`(`project_id` ASC, `quota_type` ASC) USING BTREE,
  INDEX `idx_project_ai_quotas_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目级 AI 配额表：项目共享池，与成员个人凭证无关' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_ai_quotas
-- ----------------------------

-- ----------------------------
-- Table structure for project_atomic_capabilities
-- ----------------------------
DROP TABLE IF EXISTS `project_atomic_capabilities`;
CREATE TABLE `project_atomic_capabilities`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `atomic_capability_id` bigint UNSIGNED NOT NULL COMMENT '原子能力 ID',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_atomic`(`project_id` ASC, `atomic_capability_id` ASC) USING BTREE,
  INDEX `idx_atomic`(`atomic_capability_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目订阅原子能力关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_atomic_capabilities
-- ----------------------------
INSERT INTO `project_atomic_capabilities` VALUES (1, 1, 1, 'ACTIVE', '2025-02-15 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_atomic_capabilities` VALUES (2, 1, 2, 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_atomic_capabilities` VALUES (3, 3, 1, 'ACTIVE', '2025-06-10 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for project_knowledge_configs
-- ----------------------------
DROP TABLE IF EXISTS `project_knowledge_configs`;
CREATE TABLE `project_knowledge_configs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `kb_id` bigint UNSIGNED NOT NULL COMMENT '全局知识库 ID',
  `search_weight` decimal(3, 2) NOT NULL DEFAULT 1.00 COMMENT '检索权重（0.00-1.00）',
  `inject_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'AUTO_INJECT' COMMENT '注入方式：AUTO_INJECT 自动检索写入上下文 / ON_DEMAND 按需（工具等）/ DISABLED 禁用自动注入',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_kb`(`project_id` ASC, `kb_id` ASC) USING BTREE,
  INDEX `idx_kb`(`kb_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目继承全局知识库配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_knowledge_configs
-- ----------------------------
INSERT INTO `project_knowledge_configs` VALUES (1, 1, 1, 0.80, 'ON_DEMAND', 'ACTIVE', '2025-02-11 10:00:00', '2026-03-24 17:36:27');
INSERT INTO `project_knowledge_configs` VALUES (2, 1, 2, 1.00, 'AUTO_INJECT', 'ACTIVE', '2025-02-11 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_knowledge_configs` VALUES (3, 2, 1, 0.60, 'AUTO_INJECT', 'ACTIVE', '2025-04-02 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for project_mcp_integrations
-- ----------------------------
DROP TABLE IF EXISTS `project_mcp_integrations`;
CREATE TABLE `project_mcp_integrations`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `mcp_server_id` bigint UNSIGNED NOT NULL COMMENT 'MCP 服务器 ID',
  `custom_config` json NULL COMMENT '项目级自定义配置（覆盖默认）',
  `permission_scope` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限范围（readonly / readwrite）',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `connected_at` datetime NULL DEFAULT NULL COMMENT '接入时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_mcp`(`project_id` ASC, `mcp_server_id` ASC) USING BTREE,
  INDEX `idx_mcp_server`(`mcp_server_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目 MCP 集成启用配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_mcp_integrations
-- ----------------------------
INSERT INTO `project_mcp_integrations` VALUES (1, 3, 1, '{\"default_project_id\": 99}', 'readwrite', 'ACTIVE', '2025-06-20 10:00:00', '2025-06-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_mcp_integrations` VALUES (2, 1, 2, '{\"board\": \"OMNI\"}', 'readonly', 'ACTIVE', '2025-06-01 10:00:00', '2025-06-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_mcp_integrations` VALUES (3, 1, 3, NULL, 'readonly', 'ACTIVE', '2025-07-01 10:00:00', '2025-07-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for project_members
-- ----------------------------
DROP TABLE IF EXISTS `project_members`;
CREATE TABLE `project_members`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户 ID',
  `role` enum('ADMIN','DEVELOPER','QA','PM','GUEST') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DEVELOPER' COMMENT '项目角色简写：Admin/Developer/QA/PM/Guest',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_user`(`project_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目成员与角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_members
-- ----------------------------
INSERT INTO `project_members` VALUES (1, 1, 4, 'ADMIN', '2025-02-10 10:00:00', '2025-02-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (2, 1, 9, 'ADMIN', '2025-02-10 10:00:00', '2025-02-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (3, 1, 11, 'DEVELOPER', '2025-02-12 09:00:00', '2025-02-12 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (4, 1, 5, 'DEVELOPER', '2025-02-15 09:00:00', '2025-02-15 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (5, 1, 8, 'QA', '2025-02-18 09:00:00', '2025-02-18 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (6, 2, 7, 'ADMIN', '2025-04-01 10:00:00', '2025-04-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (7, 2, 2, 'PM', '2025-04-01 10:00:00', '2025-04-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (8, 2, 11, 'DEVELOPER', '2025-04-05 09:00:00', '2025-04-05 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (9, 3, 3, 'ADMIN', '2025-05-20 10:00:00', '2025-05-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (10, 3, 5, 'DEVELOPER', '2025-05-20 10:00:00', '2025-05-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (11, 3, 6, 'DEVELOPER', '2025-05-21 09:00:00', '2025-05-21 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (12, 3, 10, 'DEVELOPER', '2025-05-22 09:00:00', '2025-05-22 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_members` VALUES (13, 5, 1, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (14, 4, 1, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (15, 3, 1, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (16, 2, 1, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (17, 1, 1, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (18, 5, 2, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (19, 4, 2, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (20, 3, 2, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (21, 1, 2, 'ADMIN', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (22, 5, 3, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (23, 4, 3, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (24, 2, 3, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (25, 1, 3, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (26, 5, 4, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (27, 4, 4, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (28, 3, 4, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (29, 2, 4, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (30, 5, 5, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (31, 4, 5, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (32, 2, 5, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (33, 5, 6, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (34, 4, 6, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (35, 2, 6, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (36, 1, 6, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (37, 5, 7, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (38, 4, 7, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (39, 3, 7, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (40, 1, 7, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (41, 5, 8, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (42, 4, 8, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (43, 3, 8, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (44, 2, 8, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (45, 5, 9, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (46, 4, 9, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (47, 3, 9, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (48, 2, 9, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (49, 5, 10, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (50, 4, 10, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (51, 2, 10, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (52, 1, 10, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (53, 5, 11, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (54, 4, 11, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (55, 3, 11, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (56, 5, 12, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (57, 4, 12, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (58, 3, 12, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (59, 2, 12, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');
INSERT INTO `project_members` VALUES (60, 1, 12, 'DEVELOPER', '2025-02-10 10:00:00', '2026-03-24 16:45:10', '2026-03-24 16:45:10');

-- ----------------------------
-- Table structure for project_skills
-- ----------------------------
DROP TABLE IF EXISTS `project_skills`;
CREATE TABLE `project_skills`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `skill_id` bigint UNSIGNED NOT NULL COMMENT '技能 ID',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_skill`(`project_id` ASC, `skill_id` ASC) USING BTREE,
  INDEX `idx_skill`(`skill_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目技能启用配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_skills
-- ----------------------------
INSERT INTO `project_skills` VALUES (1, 1, 1, 'ACTIVE', '2025-03-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_skills` VALUES (2, 2, 2, 'ACTIVE', '2025-04-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_skills` VALUES (3, 3, 3, 'ACTIVE', '2025-06-15 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_skills` VALUES (4, 3, 2, 'ACTIVE', '2025-06-15 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for project_system_prompts
-- ----------------------------
DROP TABLE IF EXISTS `project_system_prompts`;
CREATE TABLE `project_system_prompts`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `prompt_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称（如\"项目上下文摘要\"）',
  `prompt_type` enum('GLOBAL_INJECT','PROJECT_CONTEXT','CODING_STANDARD','SECURITY_RULES','CUSTOM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Prompt 类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Prompt 内容（支持 {{变量}} 语法）',
  `inject_strategy` enum('ALWAYS','ON_DEMAND','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ALWAYS' COMMENT '注入策略',
  `max_tokens` int UNSIGNED NOT NULL DEFAULT 500 COMMENT '最大注入 Token 数',
  `priority` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '注入顺序优先级',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_type`(`prompt_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目 System Prompt 注入配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_system_prompts
-- ----------------------------
INSERT INTO `project_system_prompts` VALUES (1, 1, '客服语气与合规', 'SECURITY_RULES', '禁止承诺未授权的补偿；遇敏感词转人工。', 'ALWAYS', 400, 10, 'ACTIVE', 4, '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_system_prompts` VALUES (2, 1, '项目背景摘要', 'PROJECT_CONTEXT', 'Omni-CS 覆盖 IM/邮件/电话摘要，主 SLA 为首次响应 60s。', 'ON_DEMAND', 300, 5, 'ACTIVE', 9, '2025-03-05 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_system_prompts` VALUES (3, 3, '代码风格（Java）', 'CODING_STANDARD', '使用 Spring 6 风格构造注入；Controller 禁止业务逻辑。', 'ALWAYS', 350, 8, 'ACTIVE', 3, '2025-06-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for project_templates
-- ----------------------------
DROP TABLE IF EXISTS `project_templates`;
CREATE TABLE `project_templates`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板描述',
  `template_type` enum('WORKFLOW','SKILL','SERVICE','ENVIRONMENT','CODE_SNIPPET','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `scope` enum('GLOBAL','PROJECT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'GLOBAL' COMMENT '作用域',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属项目 ID（全局为 NULL）',
  `language` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编程语言',
  `framework` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '技术框架',
  `template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板内容',
  `download_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用次数',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`template_type` ASC) USING BTREE,
  INDEX `idx_scope`(`scope` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板库表（代码模板 / 工作流模板等）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_templates
-- ----------------------------
INSERT INTO `project_templates` VALUES (1, 'FastAPI RAG 服务脚手架', '含向量入库与检索路由', 'CODE_SNIPPET', 'GLOBAL', NULL, 'Python', 'FastAPI', '# fastapi_rag_minimal\nfrom fastapi import FastAPI\napp = FastAPI()', 128, 3, '2025-04-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_templates` VALUES (2, '网关租户限流策略片段', 'Spring Cloud Gateway + Redis', 'CODE_SNIPPET', 'PROJECT', 3, 'Java', 'Spring', '// Bucket4j 配置示例\n// ...', 42, 3, '2025-08-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for project_tools
-- ----------------------------
DROP TABLE IF EXISTS `project_tools`;
CREATE TABLE `project_tools`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `tool_id` bigint UNSIGNED NOT NULL COMMENT '工具 ID',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_tool`(`project_id` ASC, `tool_id` ASC) USING BTREE,
  INDEX `idx_tool`(`tool_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目工具启用配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of project_tools
-- ----------------------------
INSERT INTO `project_tools` VALUES (1, 1, 1, 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_tools` VALUES (2, 1, 2, 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_tools` VALUES (3, 3, 1, 'ACTIVE', '2025-06-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `project_tools` VALUES (4, 3, 3, 'ACTIVE', '2025-06-10 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目编码（唯一标识，如 proj_mall）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目描述',
  `icon` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目图标（emoji 或 icon name）',
  `project_type` enum('PRODUCT','PLATFORM','DATA','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PRODUCT' COMMENT '项目类型',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者 ID',
  `owner_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目负责人 ID',
  `status` enum('ACTIVE','ARCHIVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '与 Project 实体一致',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `monthly_token_quota` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '项目月度 Token 池上限（Token 数），0=不限制',
  `used_tokens_this_month` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '项目当月已消耗 Token 数，每月初重置为 0',
  `alert_threshold_pct` tinyint UNSIGNED NOT NULL DEFAULT 80 COMMENT '项目池用量告警阈值百分比（0-100），默认 80',
  `over_quota_strategy` enum('BLOCK','ALLOW_WITH_ALERT','DOWNGRADE_MODEL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'BLOCK' COMMENT '项目池超配额策略：BLOCK 拒绝；ALLOW_WITH_ALERT 放行并告警；DOWNGRADE_MODEL 切换低成本模型',
  `last_quota_reset_at` datetime NULL DEFAULT NULL COMMENT '项目池配额最近一次月度重置时间',
  `quota_reset_cycle` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MONTHLY' COMMENT '任务/展示用配额周期：DAILY|WEEKLY|MONTHLY',
  `single_request_token_cap` bigint UNSIGNED NULL DEFAULT NULL COMMENT '单次请求 Token 上限，NULL 表示使用平台默认（如 100K）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_owner`(`owner_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of projects
-- ----------------------------
INSERT INTO `projects` VALUES (1, '统一客户服务平台1', 'proj_omni_cs', '全渠道智能客服：工单、IM、电话摘要与知识库 RAG', '🎧', 'PRODUCT', 2, 4, 'ACTIVE', '2025-02-10 10:00:00', '2026-03-20 23:45:00', 1000, 0, 80, 'BLOCK', NULL, 'MONTHLY', NULL);
INSERT INTO `projects` VALUES (2, '供应链控制塔', 'proj_supply_tower', '需求预测、库存优化与异常预警（NL2SQL 报表）', '📦', 'DATA', 2, 7, 'ACTIVE', '2025-04-01 10:00:00', '2026-03-20 23:45:00', 100, 0, 80, 'BLOCK', NULL, 'MONTHLY', NULL);
INSERT INTO `projects` VALUES (3, '研发效能与代码助手', 'proj_dev_excel', '内部 Copilot：代码评审、单测生成、流水线解释', '⚡', 'PLATFORM', 1, 3, 'ACTIVE', '2025-05-20 10:00:00', '2026-03-20 23:45:00', 0, 0, 80, 'BLOCK', NULL, 'MONTHLY', NULL);
INSERT INTO `projects` VALUES (4, '测试项目', 'TEST', '1', NULL, 'PRODUCT', NULL, NULL, 'ACTIVE', '2026-03-22 22:06:01', '2026-03-22 22:06:01', 500000, 0, 80, 'BLOCK', NULL, 'MONTHLY', NULL);
INSERT INTO `projects` VALUES (5, '测试', 'CODE', NULL, NULL, 'PRODUCT', NULL, NULL, 'ACTIVE', '2026-03-22 22:06:36', '2026-03-22 22:06:36', 500000, 0, 80, 'BLOCK', NULL, 'MONTHLY', NULL);

-- ----------------------------
-- Table structure for provider_api_keys
-- ----------------------------
DROP TABLE IF EXISTS `provider_api_keys`;
CREATE TABLE `provider_api_keys`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `provider_id` bigint UNSIGNED NOT NULL COMMENT '供应商 ID',
  `label` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标识名称（如 Anthropic 主力 Key）',
  `key_prefix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Key 前缀用于展示（如 sk-ant-...3f8k）',
  `api_key_encrypted` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密存储的真实 API Key',
  `models_allowed` json NULL COMMENT '可用模型列表（JSON 数组）',
  `monthly_quota_tokens` bigint UNSIGNED NULL DEFAULT NULL COMMENT '月 Token 配额',
  `used_tokens_month` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '本月已用 Token',
  `rate_limit_rpm` int UNSIGNED NULL DEFAULT NULL COMMENT '速率限制：每分钟请求数',
  `rate_limit_tpm` int UNSIGNED NULL DEFAULT NULL COMMENT '速率限制：每分钟 Token 数',
  `proxy_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理端点（如 /proxy/anthropic）',
  `status` enum('ACTIVE','REVOKED','EXPIRED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_provider`(`provider_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台托管的上游 API 密钥表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of provider_api_keys
-- ----------------------------
INSERT INTO `provider_api_keys` VALUES (1, 2, 'Anthropic 生产主 Key', 'sk-ant-api03-…9f2a', 'enc:v1:BASE64_PLACEHOLDER_ANTHROPIC_MAIN', '[\"claude-sonnet-4-20250514\", \"claude-3-5-haiku-20241022\"]', 80000000, 12843000, 600, 180000, '/gateway/v1/anthropic', 'ACTIVE', '2025-01-08 11:00:00', '2026-03-20 23:45:00');
INSERT INTO `provider_api_keys` VALUES (2, 2, 'Anthropic 备用 Key（华东）', 'sk-ant-api03-…c81d', 'enc:v1:BASE64_PLACEHOLDER_ANTHROPIC_DR', '[\"claude-sonnet-4-20250514\"]', 40000000, 2100000, 300, 90000, '/gateway/v1/anthropic-dr', 'ACTIVE', '2025-02-01 11:00:00', '2026-03-20 23:45:00');
INSERT INTO `provider_api_keys` VALUES (3, 1, 'OpenAI 企业主 Key', 'sk-proj-…Lm4x', 'enc:v1:BASE64_PLACEHOLDER_OPENAI_MAIN', '[\"gpt-4.1\", \"gpt-4.1-mini\"]', 120000000, 45200000, 500, 200000, '/gateway/v1/openai', 'ACTIVE', '2025-01-08 11:00:00', '2026-03-20 23:45:00');
INSERT INTO `provider_api_keys` VALUES (4, 3, 'DeepSeek 成本优化池', 'sk-ds-…p7q2', 'enc:v1:BASE64_PLACEHOLDER_DEEPSEEK', '[\"deepseek-chat\"]', 200000000, 89000000, 800, 400000, '/gateway/v1/deepseek', 'ACTIVE', '2025-03-02 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `provider_api_keys` VALUES (5, 4, 'SiliconFlow 推理备用', 'sk-sf-…k3mn', 'enc:v1:BASE64_PLACEHOLDER_SF', '[\"Qwen/Qwen2.5-72B-Instruct\"]', 50000000, 3200000, 200, 120000, '/gateway/v1/siliconflow', 'ACTIVE', '2025-04-12 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for provider_failover_policies
-- ----------------------------
DROP TABLE IF EXISTS `provider_failover_policies`;
CREATE TABLE `provider_failover_policies`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '策略名称',
  `primary_key_id` bigint UNSIGNED NOT NULL COMMENT '主用上游 Key ID',
  `fallback_key_id` bigint UNSIGNED NOT NULL COMMENT '备用上游 Key ID',
  `trigger_condition` enum('ERROR_RATE','LATENCY','QUOTA_EXCEEDED','MANUAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发条件',
  `trigger_threshold` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发阈值（如 error_rate>5%）',
  `auto_recovery` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否自动恢复',
  `status` enum('ACTIVE','DISABLED','TRIGGERED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `last_triggered_at` datetime NULL DEFAULT NULL COMMENT '最后触发时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_primary_key`(`primary_key_id` ASC) USING BTREE,
  INDEX `idx_fallback_key`(`fallback_key_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商故障转移策略表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of provider_failover_policies
-- ----------------------------
INSERT INTO `provider_failover_policies` VALUES (1, 'Anthropic 主备自动切换', 1, 2, 'ERROR_RATE', 'error_rate>3%', 1, 'ACTIVE', NULL, '2025-02-01 12:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for rbac_role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `rbac_role_permissions`;
CREATE TABLE `rbac_role_permissions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码，关联 platform_roles.role_code',
  `permission_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码，关联 permission_definitions.permission_code',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_code` ASC, `permission_code` ASC) USING BTREE,
  INDEX `idx_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联矩阵表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rbac_role_permissions
-- ----------------------------

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` bigint UNSIGNED NOT NULL COMMENT '角色 ID',
  `permission_id` bigint UNSIGNED NOT NULL COMMENT '权限点 ID',
  `access_level` enum('NONE','VIEW','CALL','CREATE','FULL_CONTROL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NONE' COMMENT '访问级别',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_permission`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 59 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (1, 1, 1, 'FULL_CONTROL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (2, 1, 2, 'FULL_CONTROL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (3, 1, 3, 'FULL_CONTROL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (4, 1, 4, 'FULL_CONTROL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (5, 1, 5, 'FULL_CONTROL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (6, 1, 6, 'FULL_CONTROL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (7, 2, 1, 'VIEW', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (8, 2, 4, 'CALL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (9, 2, 5, 'CREATE', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (10, 3, 1, 'VIEW', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (11, 3, 2, 'CREATE', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (12, 3, 3, 'FULL_CONTROL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (13, 3, 4, 'CALL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (14, 4, 1, 'VIEW', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (15, 4, 4, 'CALL', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (16, 5, 1, 'VIEW', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `role_permissions` VALUES (17, 6, 26, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (18, 6, 25, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (19, 6, 27, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (20, 6, 23, 'CREATE', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (21, 6, 22, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (22, 6, 24, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (23, 6, 6, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (24, 6, 12, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (25, 6, 13, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (26, 6, 2, 'CREATE', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (27, 6, 1, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (28, 6, 20, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (29, 6, 19, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (30, 6, 21, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (31, 6, 15, 'CREATE', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (32, 6, 16, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (33, 6, 14, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (34, 6, 18, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (35, 6, 5, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (36, 6, 3, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (37, 6, 4, 'CALL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (38, 6, 8, 'CREATE', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (39, 6, 10, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (40, 6, 9, 'FULL_CONTROL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (41, 6, 11, 'CREATE', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (42, 6, 7, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (48, 7, 1, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (49, 7, 19, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (50, 7, 14, 'VIEW', '2026-03-23 10:44:11', '2026-03-23 10:44:11');
INSERT INTO `role_permissions` VALUES (51, 7, 4, 'CALL', '2026-03-23 10:44:11', '2026-03-23 10:44:11');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称（如 超级管理员 / Developer）',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码（如 SUPER_ADMIN / DEVELOPER）',
  `role_scope` enum('PLATFORM','PROJECT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色适用范围',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `is_system` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置（不可删除）',
  `default_quota_tokens` bigint UNSIGNED NULL DEFAULT NULL COMMENT '默认月 Token 配额',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_scope`(`role_scope` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色定义表（平台角色 + 项目角色）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, '超级管理员', 'SUPER_ADMIN', 'PLATFORM', '全平台配置与用户管理，代码层拥有全部权限', 1, NULL, 'ACTIVE', '2024-06-01 09:00:00', '2026-03-23 10:59:29');
INSERT INTO `roles` VALUES (2, '平台运营', 'PLATFORM_OPS', 'PLATFORM', '模型、密钥、告警与审计', 1, 50000000, 'ACTIVE', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `roles` VALUES (3, '项目管理员', 'PROJECT_ADMIN', 'PROJECT', '项目成员、技能与工具配置', 0, 10000000, 'ACTIVE', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `roles` VALUES (4, '开发者', 'DEVELOPER', 'PROJECT', '日常开发与 AI 调用', 0, 3000000, 'ACTIVE', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `roles` VALUES (5, '只读访客', 'VIEWER', 'PROJECT', '只读项目信息', 0, 0, 'ACTIVE', '2024-06-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `roles` VALUES (6, '平台管理员', 'PLATFORM_ADMIN', 'PLATFORM', '模型、密钥、告警与审计管理', 1, NULL, 'ACTIVE', '2026-03-23 10:44:09', '2026-03-23 10:59:29');
INSERT INTO `roles` VALUES (7, '普通成员', 'MEMBER', 'PLATFORM', '日常 AI 能力使用，受配额限制', 1, NULL, 'ACTIVE', '2026-03-23 10:44:09', '2026-03-23 10:59:29');

-- ----------------------------
-- Table structure for security_events
-- ----------------------------
DROP TABLE IF EXISTS `security_events`;
CREATE TABLE `security_events`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联项目 ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联用户 ID',
  `event_type` enum('AUTHENTICATION_FAILURE','AUTHORIZATION_FAILURE','MALICIOUS_ACTIVITY','SUSPICIOUS_PATTERN','CONFIGURATION_CHANGE','API_ABUSE','CREDENTIAL_LEAK') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件类型',
  `severity` enum('CRITICAL','HIGH','MEDIUM','LOW','INFO') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件描述',
  `ai_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI 自动分析结果',
  `action_taken` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '已采取措施',
  `status` enum('DETECTED','INVESTIGATING','RESOLVED','CLOSED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DETECTED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_type`(`event_type` ASC) USING BTREE,
  INDEX `idx_severity`(`severity` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全事件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of security_events
-- ----------------------------
INSERT INTO `security_events` VALUES (1, NULL, NULL, 'API_ABUSE', 'MEDIUM', '单 IP 在 1 分钟内触发 1200 次 chat 请求', '疑似脚本未退避重试', '已临时封禁 IP 30 分钟', 'RESOLVED', '2026-03-10 11:00:00', '2026-03-20 23:45:00');
INSERT INTO `security_events` VALUES (2, 1, 11, 'SUSPICIOUS_PATTERN', 'LOW', '同一工单连续 20 次相似摘要请求', '可能自动化测试未关闭', '已通知用户 11 自查客户端配置', 'INVESTIGATING', '2026-03-19 20:10:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for security_rules
-- ----------------------------
DROP TABLE IF EXISTS `security_rules`;
CREATE TABLE `security_rules`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `rule_type` enum('IP_WHITELIST','IP_BLACKLIST','RATE_LIMIT','CONTENT_FILTER','AUTHENTICATION_POLICY','ENCRYPTION_POLICY','AUDIT_POLICY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rule_expression` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则表达式（JSON）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则描述',
  `priority` tinyint UNSIGNED NOT NULL DEFAULT 0,
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rule_name`(`rule_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安全规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of security_rules
-- ----------------------------
INSERT INTO `security_rules` VALUES (1, '办公网段白名单', 'IP_WHITELIST', '{\"cidrs\": [\"10.0.0.0/8\", \"172.16.0.0/12\"]}', '管理后台仅内网访问', 100, 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `security_rules` VALUES (2, '平台网关每用户 RPM', 'RATE_LIMIT', '{\"rpm\": 600}', '防止误循环调用', 50, 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for service_mcp_configs
-- ----------------------------
DROP TABLE IF EXISTS `service_mcp_configs`;
CREATE TABLE `service_mcp_configs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `service_id` bigint UNSIGNED NOT NULL COMMENT '服务 ID',
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `mcp_endpoint` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MCP 端点地址',
  `auth_token_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证 Token 的 Hash',
  `exposed_tools` json NULL COMMENT '暴露的工具列表（JSON）',
  `exposed_resources` json NULL COMMENT '暴露的资源列表（JSON）',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `last_checked_at` datetime NULL DEFAULT NULL COMMENT '最后连通性检查',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_service`(`service_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务 MCP Server 配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_mcp_configs
-- ----------------------------
INSERT INTO `service_mcp_configs` VALUES (1, 7, 3, 'https://devgw.cqcdi.tech/mcp/copilot-gateway', '81bcdb19233c9570e8077f23cfff5d8c53fa310dd9de0c44260e9fa9361be2ae', '[\"route_model\", \"audit_echo\"]', NULL, 'ACTIVE', '2026-03-20 07:00:00', '2025-08-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for services
-- ----------------------------
DROP TABLE IF EXISTS `services`;
CREATE TABLE `services`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '所属项目 ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务描述',
  `git_repo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Git 仓库地址',
  `main_branch` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'main' COMMENT '主分支',
  `framework` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '技术框架（Spring Boot / Vue 等）',
  `language` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编程语言',
  `status` enum('ACTIVE','ARCHIVED','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目服务与代码仓库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of services
-- ----------------------------
INSERT INTO `services` VALUES (1, 1, 'cs-conversation-api', '会话编排与多轮状态机', 'https://git.cqcdi.tech/omni/cs-conversation-api', 'main', 'Spring Boot 3.3', 'Java', 'ACTIVE', '2025-02-15 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `services` VALUES (2, 1, 'cs-rag-indexer', '文档解析、切分与向量入库', 'https://git.cqcdi.tech/omni/cs-rag-indexer', 'main', 'FastAPI', 'Python', 'ACTIVE', '2025-02-20 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `services` VALUES (3, 1, 'cs-agent-console', '客服坐席工作台前端', 'https://git.cqcdi.tech/omni/cs-agent-console', 'main', 'Vue 3 + Vite', 'TypeScript', 'ACTIVE', '2025-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `services` VALUES (4, 2, 'tower-metrics-api', '指标服务与权限下的 NL2SQL', 'https://git.cqcdi.tech/supply/tower-metrics-api', 'main', 'Spring Boot 3.3', 'Java', 'ACTIVE', '2025-04-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `services` VALUES (5, 2, 'tower-bi-web', '控制塔可视化', 'https://git.cqcdi.tech/supply/tower-bi-web', 'main', 'React 18', 'TypeScript', 'ACTIVE', '2025-04-12 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `services` VALUES (6, 3, 'dev-copilot-plugin', 'IDE 插件与本地 LSP 桥接', 'https://git.cqcdi.tech/devexcel/dev-copilot-plugin', 'main', 'Kotlin', 'Kotlin', 'ACTIVE', '2025-06-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `services` VALUES (7, 3, 'dev-copilot-gateway', '统一模型路由与审计', 'https://git.cqcdi.tech/devexcel/dev-copilot-gateway', 'main', 'Spring Boot 3.3', 'Java', 'ACTIVE', '2025-06-05 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for skill_feedback
-- ----------------------------
DROP TABLE IF EXISTS `skill_feedback`;
CREATE TABLE `skill_feedback`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `skill_id` bigint UNSIGNED NOT NULL COMMENT '技能 ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '反馈用户 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '使用的项目 ID',
  `rating` enum('UP','DOWN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评价',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '反馈备注',
  `usage_event_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联的用量事件 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_skill`(`skill_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技能使用反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of skill_feedback
-- ----------------------------
INSERT INTO `skill_feedback` VALUES (1, 1, 11, 1, 'UP', '摘要准确，节省了复制粘贴', 2, '2026-03-20 09:50:00');
INSERT INTO `skill_feedback` VALUES (2, 1, 5, 1, 'DOWN', '个别工单漏掉客户手机号', 5, '2026-03-19 22:45:00');
INSERT INTO `skill_feedback` VALUES (3, 3, 5, 3, 'UP', '评审意见比默认模板更贴近我们仓库', 4, '2026-03-20 07:25:00');

-- ----------------------------
-- Table structure for skills
-- ----------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `skill_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '技能唯一标识（如 code-review）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '技能名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '技能描述',
  `scope` enum('GLOBAL','PROJECT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作用域',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属项目 ID（全局为 NULL）',
  `category` enum('ENGINEERING','QUALITY','SECURITY','DOCUMENTATION','BUSINESS','AI_NATIVE','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OTHER' COMMENT '技能分类',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '角色 Prompt 模板（支持 {{变量}} 语法）',
  `knowledge_refs` json NULL COMMENT '关联知识库引用（JSON 数组）',
  `bound_tools` json NULL COMMENT '绑定工具列表（JSON 数组）',
  `parameters` json NULL COMMENT '用户可配参数定义（JSON 数组）',
  `slash_command` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发命令（如 /code-review）',
  `version` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1.0.0' COMMENT '技能版本',
  `status` enum('DRAFT','PUBLISHED','DEPRECATED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DRAFT' COMMENT '发布状态',
  `usage_count` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计使用次数',
  `satisfaction_up` int UNSIGNED NOT NULL DEFAULT 0,
  `satisfaction_down` int UNSIGNED NOT NULL DEFAULT 0,
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_skill_key`(`skill_key` ASC) USING BTREE,
  INDEX `idx_scope`(`scope` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '技能定义表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of skills
-- ----------------------------
INSERT INTO `skills` VALUES (1, 'ticket-summarize', '工单摘要', '将冗长工单对话压缩为结构化摘要与下一步建议', 'PROJECT', 1, 'AI_NATIVE', '你是资深客服主管。请用中文输出：问题摘要、情绪、建议动作。', '[\"kb:3\"]', '[\"search_kb\"]', '[{\"name\": \"tone\", \"type\": \"string\", \"default\": \"neutral\"}]', '/ticket-sum', '1.2.0', 'PUBLISHED', 18420, 420, 18, 4, '2025-03-01 10:00:00', '2026-03-20 23:45:00', '2025-04-01 10:00:00');
INSERT INTO `skills` VALUES (2, 'sql-guardrail', '安全 SQL 助手', '在受控 schema 下生成只读 SQL 并解释', 'GLOBAL', NULL, 'ENGINEERING', '仅允许 SELECT/WITH，拒绝 DDL/DML。', '[\"kb:1\"]', '[]', '[]', '/sql-help', '1.0.3', 'PUBLISHED', 9021, 210, 9, 3, '2025-04-20 10:00:00', '2026-03-20 23:45:00', '2025-05-01 10:00:00');
INSERT INTO `skills` VALUES (3, 'pr-review', 'Pull Request 评审', '按公司规范给出风险点与测试建议', 'PROJECT', 3, 'QUALITY', '你是严格但友好的代码评审员。', '[]', '[\"tool_fetch_diff\"]', '[{\"name\": \"strictness\", \"type\": \"string\", \"default\": \"normal\"}]', '/pr-review', '2.0.1', 'PUBLISHED', 5622, 305, 22, 3, '2025-06-10 10:00:00', '2026-03-20 23:45:00', '2025-07-01 10:00:00');

-- ----------------------------
-- Table structure for sprints
-- ----------------------------
DROP TABLE IF EXISTS `sprints`;
CREATE TABLE `sprints`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '迭代名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '迭代描述',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `status` enum('PLANNED','ACTIVE','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PLANNED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '迭代（Sprint）表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sprints
-- ----------------------------
INSERT INTO `sprints` VALUES (1, 1, '2026.3.2 客服迭代', 'RAG 召回与质检抽样', '2026-03-10', '2026-03-23', 'ACTIVE', '2026-03-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `sprints` VALUES (2, 3, '2026.3.2 平台迭代', '网关 P95 优化', '2026-03-10', '2026-03-24', 'ACTIVE', '2026-03-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `epic_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT 'Epic ID',
  `sprint_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '迭代 ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务描述',
  `priority` enum('CRITICAL','HIGH','NORMAL','LOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NORMAL',
  `status` enum('OPEN','IN_PROGRESS','DONE','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OPEN',
  `assignee_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '负责人 ID',
  `story_points` int UNSIGNED NULL DEFAULT NULL COMMENT '故事点数',
  `due_date` date NULL DEFAULT NULL COMMENT '截止日期',
  `created_by` bigint UNSIGNED NULL DEFAULT NULL COMMENT '创建者 ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_epic`(`epic_id` ASC) USING BTREE,
  INDEX `idx_sprint`(`sprint_id` ASC) USING BTREE,
  INDEX `idx_assignee`(`assignee_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务/用户故事表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tasks
-- ----------------------------
INSERT INTO `tasks` VALUES (1, 1, 1, 1, '图片工单 OCR 置信度低于 0.85 时转人工', '与现有路由表打通', 'HIGH', 'IN_PROGRESS', 11, 5, '2026-03-21', 4, '2026-03-10 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `tasks` VALUES (2, 1, 1, 1, '质检抽样策略：按渠道分层', NULL, 'NORMAL', 'OPEN', 8, 3, '2026-03-22', 4, '2026-03-11 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `tasks` VALUES (3, 3, 3, 2, '网关导出 OpenTelemetry 与租户标签', NULL, 'CRITICAL', 'IN_PROGRESS', 5, 8, '2026-03-20', 3, '2026-03-12 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `tasks` VALUES (4, 3, 3, 2, '文档：Copilot 插件故障排查', NULL, 'LOW', 'DONE', 6, 2, '2026-03-15', 3, '2026-03-05 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for tool_definitions
-- ----------------------------
DROP TABLE IF EXISTS `tool_definitions`;
CREATE TABLE `tool_definitions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `tool_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工具名称（唯一，AI 调用时使用）',
  `display_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '展示名称（中文）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '工具描述（影响 AI 调用决策）',
  `scope` enum('BUILTIN','GLOBAL','PROJECT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作用域',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属项目 ID（BUILTIN/GLOBAL 为 NULL）',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OTHER' COMMENT '分类',
  `input_schema` json NOT NULL COMMENT '输入参数 JSON Schema',
  `output_schema` json NULL COMMENT '输出 JSON Schema',
  `impl_type` enum('INTERNAL','HTTP_CALLBACK','MCP_PROXY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'INTERNAL' COMMENT '实现方式',
  `impl_config` json NULL COMMENT '实现配置（URL、Method、Header 等）',
  `permission_required` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '调用所需权限点',
  `audit_level` enum('NORMAL','SENSITIVE','CRITICAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NORMAL' COMMENT '审计级别',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tool_name`(`tool_name` ASC) USING BTREE,
  INDEX `idx_scope`(`scope` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工具定义表（Function Tool 注册中心）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tool_definitions
-- ----------------------------
INSERT INTO `tool_definitions` VALUES (1, 'search_kb', '检索知识库', '按 query 检索已授权知识库片段', 'GLOBAL', NULL, 'knowledge', '{\"type\": \"object\", \"required\": [\"query\"], \"properties\": {\"query\": {\"type\": \"string\"}, \"top_k\": {\"type\": \"integer\", \"default\": 5}}}', NULL, 'INTERNAL', NULL, 'knowledge.view', 'NORMAL', 'ACTIVE', '2025-01-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `tool_definitions` VALUES (2, 'create_ticket_draft', '创建工单草稿', '在 ITSM 中创建草稿工单（需审批后提交）', 'PROJECT', 1, 'project_mgmt', '{\"type\": \"object\", \"required\": [\"title\", \"body\"], \"properties\": {\"body\": {\"type\": \"string\"}, \"title\": {\"type\": \"string\"}}}', NULL, 'HTTP_CALLBACK', '{\"url\": \"https://itsm.internal.example/api/tickets/draft\", \"method\": \"POST\"}', 'tool.invoke', 'SENSITIVE', 'ACTIVE', '2025-02-20 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `tool_definitions` VALUES (3, 'fetch_diff', '获取 PR Diff', '从 GitLab 获取 Merge Request 变更', 'PROJECT', 3, 'code', '{\"type\": \"object\", \"required\": [\"mr_iid\"], \"properties\": {\"mr_iid\": {\"type\": \"integer\"}}}', NULL, 'HTTP_CALLBACK', '{\"url\": \"https://git.cqcdi.tech/api/v4/projects/99/merge_requests/{{mr_iid}}/changes\", \"method\": \"GET\"}', 'tool.invoke', 'NORMAL', 'ACTIVE', '2025-06-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for tool_invocation_logs
-- ----------------------------
DROP TABLE IF EXISTS `tool_invocation_logs`;
CREATE TABLE `tool_invocation_logs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `tool_id` bigint UNSIGNED NOT NULL COMMENT '工具 ID',
  `project_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '项目 ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '用户 ID',
  `skill_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '触发的技能 ID',
  `credential_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '使用的平台凭证 ID',
  `workflow_execution_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '工作流执行 ID',
  `input_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输入参数',
  `output_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输出结果',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `duration_ms` int UNSIGNED NULL DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `status` enum('SUCCESS','FAILED','TIMEOUT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `executed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tool`(`tool_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_executed`(`executed_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工具调用日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tool_invocation_logs
-- ----------------------------
INSERT INTO `tool_invocation_logs` VALUES (1, 1, 1, 11, 1, 2, NULL, '{\"query\":\"退换货政策 数码产品\",\"top_k\":5}', '{\"hits\":4,\"top_score\":0.82}', NULL, 118, 'SUCCESS', '2026-03-20 09:46:01', '2026-03-20 23:45:00');
INSERT INTO `tool_invocation_logs` VALUES (2, 2, 1, 5, NULL, 1, NULL, '{\"title\":\"VPN 连接失败 错误 809\",\"body\":\"Win11...\"}', '{\"draft_id\":\"IT-98231\"}', NULL, 240, 'SUCCESS', '2026-03-19 14:22:00', '2026-03-20 23:45:00');
INSERT INTO `tool_invocation_logs` VALUES (3, 3, 3, 3, 3, 3, NULL, '{\"mr_iid\":128}', '{\"files_changed\":7}', NULL, 890, 'SUCCESS', '2026-03-20 07:19:10', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for user_client_bindings
-- ----------------------------
DROP TABLE IF EXISTS `user_client_bindings`;
CREATE TABLE `user_client_bindings`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户 ID',
  `client_app_id` bigint UNSIGNED NOT NULL COMMENT '客户端 ID',
  `binding_status` enum('ACTIVE','INACTIVE','REVOKED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `last_active_at` datetime NULL DEFAULT NULL COMMENT '最后活跃时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_client`(`user_id` ASC, `client_app_id` ASC) USING BTREE,
  INDEX `idx_client`(`client_app_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户研发客户端绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_client_bindings
-- ----------------------------
INSERT INTO `user_client_bindings` VALUES (1, 5, 1, 'ACTIVE', '2026-03-19 18:00:00', '2025-08-15 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `user_client_bindings` VALUES (2, 11, 1, 'ACTIVE', '2026-03-20 10:12:00', '2025-09-01 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `user_client_bindings` VALUES (3, 6, 2, 'ACTIVE', '2026-03-18 21:30:00', '2025-10-10 09:00:00', '2026-03-20 23:45:00');
INSERT INTO `user_client_bindings` VALUES (4, 3, 3, 'ACTIVE', '2026-03-20 08:00:00', '2025-11-01 09:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱（登录标识）',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `full_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像 URL',
  `department_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '所属部门 ID',
  `job_title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `platform_role` enum('SUPER_ADMIN','PLATFORM_ADMIN','MEMBER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MEMBER' COMMENT '平台角色',
  `role_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '平台角色 ID，关联 roles.id（role_scope=PLATFORM）',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'BCrypt 哈希后的密码',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '与 User 实体一致',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_department`(`department_id` ASC) USING BTREE,
  INDEX `idx_platform_role`(`platform_role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'theo@qq.com', 'theo', '唐浩', 'https://cdn.example.com/avatar/chenyu.png', 1, '研发副总裁', '13800001001', 'SUPER_ADMIN', 1, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-06-10 10:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (2, 'liu.mei@cqcdi.tech', 'liumei', '刘梅', 'https://cdn.example.com/avatar/liumei.png', 2, '产品总监', '13800001002', 'PLATFORM_ADMIN', 6, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-06-12 10:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (3, 'zhao.lei@cqcdi.tech', 'zhaolei', '赵磊', 'https://cdn.example.com/avatar/zhaolei.png', 1, '架构师', '13800001003', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-07-01 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (4, 'sun.qian@cqcdi.tech', 'sunqian', '孙倩', 'https://cdn.example.com/avatar/sunqian.png', 3, '算法负责人', '13800001004', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-07-15 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (5, 'zhou.hao@cqcdi.tech', 'zhouhao', '周浩', 'https://cdn.example.com/avatar/zhouhao.png', 1, '高级后端工程师', '13800001005', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-08-01 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (6, 'wu.ting@cqcdi.tech', 'wuting', '吴婷', 'https://cdn.example.com/avatar/wuting.png', 1, '前端负责人', '13800001006', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-08-05 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (7, 'zheng.kai@cqcdi.tech', 'zhengkai', '郑凯', 'https://cdn.example.com/avatar/zhengkai.png', 3, '数据工程师', '13800001007', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-08-20 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (8, 'feng.nan@cqcdi.tech', 'fengnan', '冯楠', 'https://cdn.example.com/avatar/fengnan.png', 4, '测试经理', '13800001008', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-09-01 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (9, 'he.yan@cqcdi.tech', 'heyan', '何妍', 'https://cdn.example.com/avatar/heyan.png', 2, '高级产品经理', '13800001009', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-09-10 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (10, 'jiang.mo@cqcdi.tech', 'jiangmo', '江默', 'https://cdn.example.com/avatar/jiangmo.png', 5, '运维负责人', '13800001010', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-09-15 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (11, 'xie.lin@cqcdi.tech', 'xielin', '谢琳', 'https://cdn.example.com/avatar/xielin.png', 3, 'LLM 应用工程师', '13800001011', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-10-01 09:00:00', '2026-03-23 11:00:16');
INSERT INTO `users` VALUES (12, 'dong.ze@cqcdi.tech', 'dongze', '董泽', 'https://cdn.example.com/avatar/dongze.png', 6, '安全工程师', '13800001012', 'MEMBER', 7, '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', '2024-10-15 09:00:00', '2026-03-23 11:00:16');

-- ----------------------------
-- Table structure for workflow_definitions
-- ----------------------------
DROP TABLE IF EXISTS `workflow_definitions`;
CREATE TABLE `workflow_definitions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作流名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '工作流描述',
  `workflow_type` enum('STANDARD','AUTOMATION','AI_DRIVEN','EVENT_DRIVEN','SCHEDULED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `definition_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作流定义（JSON）',
  `version` int UNSIGNED NOT NULL DEFAULT 1,
  `status` enum('DRAFT','PUBLISHED','ARCHIVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DRAFT',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_type`(`workflow_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流定义表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of workflow_definitions
-- ----------------------------
INSERT INTO `workflow_definitions` VALUES (1, 1, '工单升级审批', '满足条件自动拉群并通知主管', 'AUTOMATION', '{\"nodes\":[\"triage\",\"notify\",\"wait\"],\"version\":1}', 3, 'PUBLISHED', '2025-09-01 10:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for workflow_execution_steps
-- ----------------------------
DROP TABLE IF EXISTS `workflow_execution_steps`;
CREATE TABLE `workflow_execution_steps`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `execution_id` bigint UNSIGNED NOT NULL COMMENT '执行记录 ID',
  `step_order` int UNSIGNED NOT NULL COMMENT '步骤顺序',
  `step_type` enum('TASK','DECISION','PARALLEL','WAIT','AI_ACTION','NOTIFICATION') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('PENDING','RUNNING','SUCCESS','FAILED','SKIPPED','TIMEOUT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING',
  `output_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输出数据',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_execution`(`execution_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流执行步骤追踪表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of workflow_execution_steps
-- ----------------------------
INSERT INTO `workflow_execution_steps` VALUES (1, 1, 1, 'DECISION', 'SUCCESS', '{\"branch\":\"P1\"}', NULL, '2026-03-18 19:00:00', '2026-03-18 19:00:10', '2026-03-18 19:00:00', '2026-03-20 23:45:00');
INSERT INTO `workflow_execution_steps` VALUES (2, 1, 2, 'NOTIFICATION', 'SUCCESS', '{\"message_id\":\"ww_msg_001\"}', NULL, '2026-03-18 19:00:10', '2026-03-18 19:00:45', '2026-03-18 19:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for workflow_executions
-- ----------------------------
DROP TABLE IF EXISTS `workflow_executions`;
CREATE TABLE `workflow_executions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `definition_id` bigint UNSIGNED NOT NULL COMMENT '工作流定义 ID',
  `project_id` bigint UNSIGNED NOT NULL COMMENT '项目 ID',
  `trigger_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '触发者 ID',
  `trigger_type` enum('MANUAL','WEBHOOK','SCHEDULED','AI_TRIGGERED','EVENT_DRIVEN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('PENDING','RUNNING','SUCCESS','FAILED','ABORTED','SUSPENDED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING',
  `input_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输入数据',
  `output_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输出数据',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_definition`(`definition_id` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_trigger_type`(`trigger_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流执行记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of workflow_executions
-- ----------------------------
INSERT INTO `workflow_executions` VALUES (1, 1, 1, 9, 'EVENT_DRIVEN', 'SUCCESS', '{\"ticket_id\":\"CS-88231\"}', '{\"notified\":true}', NULL, '2026-03-18 19:00:00', '2026-03-18 19:00:45', '2026-03-18 19:00:00', '2026-03-20 23:45:00');

-- ----------------------------
-- Table structure for workflow_nodes
-- ----------------------------
DROP TABLE IF EXISTS `workflow_nodes`;
CREATE TABLE `workflow_nodes`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `definition_id` bigint UNSIGNED NOT NULL COMMENT '工作流定义 ID',
  `node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `node_type` enum('TASK','DECISION','PARALLEL','WAIT','AI_ACTION','NOTIFICATION','START','END') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点配置（JSON）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_definition`(`definition_id` ASC) USING BTREE,
  INDEX `idx_type`(`node_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流节点表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of workflow_nodes
-- ----------------------------
INSERT INTO `workflow_nodes` VALUES (1, 1, '开始', 'START', '{}', '2025-09-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `workflow_nodes` VALUES (2, 1, '判断是否 P1', 'DECISION', '{\"expr\":\"priority==\"P1\"\"}', '2025-09-01 10:00:00', '2026-03-20 23:45:00');
INSERT INTO `workflow_nodes` VALUES (3, 1, '企业微信通知', 'NOTIFICATION', '{\"channel_id\":1}', '2025-09-01 10:00:00', '2026-03-20 23:45:00');

SET FOREIGN_KEY_CHECKS = 1;
