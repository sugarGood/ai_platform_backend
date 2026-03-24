# AI 平台数据库设计

## 1. 设计目标

本设计以原型和 PRD 为准，目标不是“把现有 SQL 原样说明一遍”，而是给出一套可直接支持开发的目标模型。

本轮设计重点解决四件事：

1. 让原型页面都能找到明确的数据归属。
2. 把“角色模板 + 成员覆写 + 资源授权”补齐。
3. 把“项目共享池 + 成员个人配额”双池模型落清楚。
4. 把历史 SQL 中命名冲突、语义重叠和非主链路表从主设计中拆开。

## 2. 原型驱动的数据对象

| 原型对象 | 核心含义 | 目标表 |
|---|---|---|
| 平台员工 | 平台登录主体 | `users` `departments` `platform_roles` |
| 平台凭证 | 统一接入凭证 | `platform_credentials` `credential_project_scopes` `credential_rotation_logs` |
| 客户端接入 | Claude Code / Cursor / Codex 接入 | `client_apps` `user_client_bindings` |
| 项目空间 | 工作上下文和治理边界 | `projects` `project_members` |
| 项目角色模板 | Admin / Developer / QA / PM / Guest | `project_role_templates` `project_role_template_permissions` |
| 成员能力覆写 | 成员个体权限与资源范围 | `project_member_permission_overrides` `project_member_resource_grants` |
| 项目代码服务 | 服务、环境、代码仓库 | `services` `service_environments` |
| 项目 Agent | 乐知助手等项目专属智能体 | `project_agents` `project_system_prompts` |
| AI 资源治理 | 供应商、模型、上游 Key、路由、配额策略 | `ai_providers` `ai_models` `provider_api_keys` `model_routing_policies` `global_quota_policies` |
| 双池配额 | 项目池 + 成员个人池 | `project_token_pools` `member_ai_quotas` |
| 知识库 | 全局和项目知识资产 | `knowledge_bases` `kb_documents` `project_knowledge_configs` |
| 技能库 | Prompt 模板和工作流模板 | `skills` `project_skills` `skill_feedback` |
| 工具集 | Function Tool 注册中心 | `tools` `project_tools` |
| 集成市场 | 市场目录、MCP 服务、授权态 | `integration_market_items` `mcp_servers` `mcp_server_authorizations` `project_mcp_integrations` |
| 原子能力 | 企业级能力标准与订阅 | `atomic_capabilities` `project_atomic_capabilities` |
| 模板库 | 模板建项目、建服务 | `project_templates` |
| 运行追踪 | AI 调用、知识检索、工具调用 | `ai_usage_events` `ai_usage_daily` `knowledge_search_logs` `tool_invocation_logs` |
| 审计与安全 | 活动、审计、告警、事故、安全规则 | `activity_logs` `audit_logs` `alert_rules` `alert_events` `incidents` `notification_channels` `security_rules` `security_events` |
| Agent 工作流 | 工作流定义和执行 | `workflow_definitions` `workflow_nodes` `workflow_executions` `workflow_execution_steps` |

## 3. 与历史 SQL 的收敛方案

| 历史表/问题 | 问题说明 | 目标方案 |
|---|---|---|
| `roles` | 混合平台角色和项目角色模板，难以表达原型中的两套页面 | 拆成 `platform_roles` 和 `project_role_templates` |
| `tool_definitions` | 命名偏技术实现，不利于与原型“工具集”对齐 | 收敛为 `tools` |
| `provider_failover_policies` | 只能表达主备，不足以表达“模型路由配置” | 收敛为 `model_routing_policies` |
| `project_ai_quotas` | 语义正确但命名偏泛，未突出共享池 | 收敛为 `project_token_pools` |
| `oauth_connections` | 只覆盖 OAuth，不覆盖 Bearer/API Key 等授权态 | 收敛为 `mcp_server_authorizations` |
| `platform_credential_projects` | 名称过于依赖凭证，不适合表达服务账号/临时凭证范围 | 收敛为 `credential_project_scopes` |
| 仅有模块权限，无资源范围 | 原型支持“选择具体知识库/技能/工具范围” | 增加 `project_member_resource_grants` |

## 4. 目标数据域

## 4.1 平台身份与接入域

### 4.1.1 `departments`

存储部门。

关键字段：

- `id`
- `name`
- `code`
- `description`
- `status`

### 4.1.2 `platform_roles`

仅表达平台级角色，例如超级管理员、平台管理员、普通成员。

关键字段：

- `id`
- `role_code`
- `role_name`
- `description`
- `is_system`
- `status`

### 4.1.3 `permission_definitions`

统一权限点字典。

关键字段：

- `id`
- `module_key`
- `permission_key`
- `permission_name`
- `scope_type`
- `description`

建议模块：

- `platform_user`
- `credential`
- `knowledge`
- `skill`
- `tool`
- `integration`
- `project_member`
- `quota`
- `project_setting`
- `audit`

### 4.1.4 `platform_role_permissions`

平台角色到权限点的矩阵。

关键字段：

- `id`
- `platform_role_id`
- `permission_id`
- `access_level`

### 4.1.5 `users`

平台员工。

关键字段：

- `id`
- `email`
- `username`
- `full_name`
- `department_id`
- `platform_role_id`
- `job_title`
- `status`
- `password_hash`
- `last_login_at`
- `created_at`
- `updated_at`

约束：

1. 一个用户只持有一个平台角色。
2. 平台角色变更必须写 `activity_logs` 和 `audit_logs`。

### 4.1.6 `client_apps`

客户端定义。

关键字段：

- `id`
- `code`
- `name`
- `supports_mcp`
- `supports_custom_gateway`
- `setup_template`
- `status`

### 4.1.7 `user_client_bindings`

成员客户端接入状态。

关键字段：

- `id`
- `user_id`
- `client_app_id`
- `binding_status`
- `last_active_at`

### 4.1.8 `platform_credentials`

平台凭证主表。

关键字段：

- `id`
- `credential_code`
- `subject_type`
- `subject_user_id`
- `display_name`
- `key_hash`
- `key_prefix`
- `credential_type`
- `default_project_id`
- `rate_limit_rpm`
- `status`
- `expires_at`
- `last_used_at`
- `last_used_ip`
- `revoked_at`
- `revoke_reason`
- `created_by`
- `created_at`
- `updated_at`

设计说明：

1. `subject_type` 取值建议：`USER` `SERVICE_ACCOUNT` `TEMPORARY`。
2. 个人凭证默认一人一张。
3. `default_project_id` 对应“当前工作项目”。
4. 服务账号和临时凭证允许不绑定真实用户，但必须绑定创建者和项目范围。

### 4.1.9 `credential_project_scopes`

显式声明凭证可用项目范围。

关键字段：

- `id`
- `credential_id`
- `project_id`
- `scope_source`
- `created_at`

说明：

1. 个人凭证可由成员项目关系自动推导，也可做显式快照。
2. 服务账号和临时凭证必须显式绑定项目范围。

### 4.1.10 `credential_rotation_logs`

记录平台凭证和上游 Key 的轮换日志。

关键字段：

- `id`
- `target_type`
- `target_id`
- `rotation_type`
- `old_key_prefix`
- `new_key_prefix`
- `result`
- `error_message`
- `operated_by`
- `created_at`

## 4.2 项目治理域

### 4.2.1 `projects`

项目主表。

关键字段：

- `id`
- `code`
- `name`
- `description`
- `icon`
- `project_type`
- `owner_user_id`
- `current_status`
- `default_model_id`
- `default_prompt_profile`
- `status`
- `created_by`
- `created_at`
- `updated_at`

### 4.2.2 `project_role_templates`

项目角色模板，不与平台角色混用。

关键字段：

- `id`
- `project_id`
- `role_key`
- `role_name`
- `description`
- `is_default`
- `default_quota_tokens`
- `status`
- `created_at`
- `updated_at`

设计说明：

1. 每个项目可复制平台默认模板后做项目内自定义。
2. 至少保留五个默认模板：`ADMIN` `DEVELOPER` `QA` `PM` `GUEST`。

### 4.2.3 `project_role_template_permissions`

项目角色模板对模块权限的默认矩阵。

关键字段：

- `id`
- `project_role_template_id`
- `module_key`
- `access_level`
- `approval_required`
- `created_at`

建议模块：

- `knowledge`
- `skill`
- `tool`
- `integration`
- `member`
- `quota`
- `project_setting`
- `credential`
- `incident`

### 4.2.4 `project_members`

项目成员关系。

关键字段：

- `id`
- `project_id`
- `user_id`
- `project_role_template_id`
- `member_status`
- `joined_at`
- `updated_at`

### 4.2.5 `project_member_permission_overrides`

成员级权限覆写。

关键字段：

- `id`
- `project_member_id`
- `module_key`
- `access_level`
- `approval_required`
- `source_type`
- `created_by`
- `created_at`
- `updated_at`

### 4.2.6 `project_member_resource_grants`

成员可用能力的资源范围授权。

关键字段：

- `id`
- `project_member_id`
- `resource_type`
- `resource_id`
- `grant_mode`
- `created_by`
- `created_at`

资源类型建议：

- `KNOWLEDGE_BASE`
- `SKILL`
- `TOOL`
- `MCP_INTEGRATION`

### 4.2.7 `services`

项目代码服务。

关键字段：

- `id`
- `project_id`
- `service_code`
- `name`
- `description`
- `git_repo_url`
- `main_branch`
- `framework`
- `language`
- `status`

### 4.2.8 `service_environments`

服务环境。

关键字段：

- `id`
- `service_id`
- `project_id`
- `env_name`
- `env_type`
- `url`
- `current_branch`
- `current_version`
- `health_status`
- `status`

### 4.2.9 `project_system_prompts`

项目 Prompt 资产。

关键字段：

- `id`
- `project_id`
- `prompt_name`
- `prompt_type`
- `content`
- `inject_strategy`
- `max_tokens`
- `priority`
- `status`
- `created_by`

### 4.2.10 `project_agents`

项目专属 Agent。

关键字段：

- `id`
- `project_id`
- `agent_name`
- `agent_description`
- `agent_avatar`
- `system_prompt`
- `default_model_id`
- `enable_knowledge`
- `enable_skills`
- `enable_tools`
- `status`

## 4.3 AI 资源治理域

### 4.3.1 `ai_providers`

上游供应商。

### 4.3.2 `ai_models`

模型目录。

关键字段：

- `provider_id`
- `model_code`
- `display_name`
- `model_family`
- `context_window`
- `input_price_per_1m`
- `output_price_per_1m`

### 4.3.3 `provider_api_keys`

平台托管的真实上游 Key。

关键字段：

- `id`
- `provider_id`
- `label`
- `key_prefix`
- `api_key_encrypted`
- `models_allowed`
- `monthly_quota_tokens`
- `used_tokens_month`
- `rate_limit_rpm`
- `rate_limit_tpm`
- `proxy_endpoint`
- `status`

### 4.3.4 `model_routing_policies`

模型路由策略。

关键字段：

- `id`
- `policy_name`
- `model_id`
- `primary_provider_api_key_id`
- `fallback_provider_api_key_id`
- `trigger_condition`
- `trigger_threshold`
- `auto_recovery`
- `status`

### 4.3.5 `global_quota_policies`

平台全局配额策略。

关键字段：

- `id`
- `subject_type`
- `subject_key`
- `token_limit`
- `cost_limit`
- `request_limit`
- `reset_cycle`
- `alert_threshold_percent`
- `status`

### 4.3.6 `project_token_pools`

项目共享池。

关键字段：

- `id`
- `project_id`
- `quota_type`
- `quota_limit`
- `used_amount`
- `reset_cycle`
- `last_reset_at`
- `exhaust_behavior`
- `alert_threshold_percent`
- `status`

说明：

1. 这是原型“项目 Token 池”的直接落表。
2. 同一项目可扩展 `TOKEN`、`COST`、`REQUEST` 多种池类型。

### 4.3.7 `member_ai_quotas`

成员个人配额。

关键字段：

- `id`
- `user_id`
- `project_id`
- `quota_type`
- `quota_limit`
- `used_amount`
- `reset_cycle`
- `last_reset_at`
- `status`

说明：

1. `project_id IS NULL` 表示成员平台总配额。
2. `project_id IS NOT NULL` 表示成员在项目内的个人配额。

## 4.4 AI 能力资产域

### 4.4.1 `knowledge_bases`

知识库容器。

关键字段：

- `id`
- `name`
- `description`
- `scope`
- `project_id`
- `category`
- `embedding_model`
- `doc_count`
- `total_chunks`
- `hit_rate`
- `inject_mode`
- `status`
- `created_by`

### 4.4.2 `kb_documents`

知识库文档。

关键字段：

- `id`
- `kb_id`
- `title`
- `source_type`
- `file_type`
- `file_path`
- `file_size`
- `chunk_count`
- `hit_count`
- `inject_mode`
- `status`
- `error_message`
- `uploaded_by`
- `created_at`
- `updated_at`

### 4.4.3 `project_knowledge_configs`

项目继承知识库和检索权重配置。

关键字段：

- `id`
- `project_id`
- `knowledge_base_id`
- `inherit_mode`
- `search_weight`
- `inject_strategy`
- `status`

### 4.4.4 `skills`

技能主表。

关键字段：

- `id`
- `skill_key`
- `name`
- `description`
- `scope`
- `project_id`
- `category`
- `system_prompt`
- `knowledge_refs`
- `bound_tools`
- `parameters`
- `slash_command`
- `version`
- `status`
- `usage_count`
- `satisfaction_up`
- `satisfaction_down`
- `created_by`
- `published_at`

### 4.4.5 `project_skills`

项目启用的技能集合。

说明：

1. 项目自有技能通过 `skills.scope='PROJECT'` 表达。
2. 全局技能是否在项目中启用，通过 `project_skills` 表达。

### 4.4.6 `skill_feedback`

技能点赞/点踩反馈。

### 4.4.7 `tools`

工具主表。

关键字段：

- `id`
- `tool_name`
- `display_name`
- `description`
- `scope`
- `project_id`
- `category`
- `input_schema`
- `output_schema`
- `impl_type`
- `impl_config`
- `permission_required`
- `audit_level`
- `status`

### 4.4.8 `project_tools`

项目启用工具集合。

### 4.4.9 `integration_market_items`

集成市场目录。

关键字段：

- `id`
- `market_key`
- `display_name`
- `category`
- `provider`
- `description`
- `icon_url`
- `doc_url`
- `supported_auth_types`
- `default_server_type`
- `status`
- `recommend_rank`

### 4.4.10 `mcp_servers`

实际可接入的 MCP 服务实例定义。

关键字段：

- `id`
- `market_item_id`
- `server_name`
- `display_name`
- `description`
- `server_type`
- `project_id`
- `category`
- `server_url`
- `auth_type`
- `capabilities`
- `status`
- `last_checked_at`

### 4.4.11 `mcp_server_authorizations`

MCP 授权态，不再局限于 OAuth。

关键字段：

- `id`
- `mcp_server_id`
- `project_id`
- `authorization_type`
- `access_token_encrypted`
- `refresh_token_encrypted`
- `secret_payload_encrypted`
- `token_expires_at`
- `scopes`
- `account_name`
- `status`
- `authorized_by`
- `authorized_at`

### 4.4.12 `project_mcp_integrations`

项目已接入集成关系。

关键字段：

- `id`
- `project_id`
- `mcp_server_id`
- `project_config`
- `permission_scope`
- `status`

### 4.4.13 `atomic_capabilities`

原子能力目录。

### 4.4.14 `project_atomic_capabilities`

项目订阅原子能力关系。

### 4.4.15 `project_templates`

模板库。

## 4.5 运行、审计与安全域

### 4.5.1 `ai_usage_events`

AI 请求级明细。

关键字段：

- `credential_id`
- `user_id`
- `project_id`
- `provider_id`
- `provider_api_key_id`
- `model_id`
- `client_app_id`
- `request_id`
- `conversation_id`
- `skill_id`
- `input_tokens`
- `output_tokens`
- `total_tokens`
- `cost_amount`
- `project_pool_check_result`
- `member_quota_check_result`
- `status`
- `error_message`
- `latency_ms`
- `occurred_at`

### 4.5.2 `ai_usage_daily`

日聚合统计。

### 4.5.3 `knowledge_search_logs`

知识检索日志。

### 4.5.4 `tool_invocation_logs`

工具调用日志。

### 4.5.5 `activity_logs`

用户可见的业务活动时间线。

### 4.5.6 `audit_logs`

敏感操作审计。

### 4.5.7 `alert_rules` / `alert_events`

告警规则和事件。

### 4.5.8 `incidents`

事故中心。

### 4.5.9 `notification_channels`

通知通道。

### 4.5.10 `security_rules` / `security_events`

安全规则和安全事件。

## 4.6 Agent 工作流域

### 4.6.1 `workflow_definitions`

工作流定义。

### 4.6.2 `workflow_nodes`

工作流节点。

### 4.6.3 `workflow_executions`

工作流执行记录。

### 4.6.4 `workflow_execution_steps`

工作流步骤执行追踪。

## 5. 页面级写入链路

## 5.1 成员首次接入

写表：

- `platform_credentials`
- `credential_project_scopes`
- `user_client_bindings`
- `activity_logs`

可选写表：

- `audit_logs`

## 5.2 项目管理员调整成员角色

写表：

- `project_members`
- `project_member_permission_overrides`
- `project_member_resource_grants`
- `member_ai_quotas`
- `activity_logs`
- `audit_logs`

## 5.3 项目管理员配置知识库继承

写表：

- `project_knowledge_configs`
- `activity_logs`

## 5.4 项目管理员发布技能

写表：

- `skills`
- `project_skills`
- `activity_logs`
- `audit_logs`

## 5.5 项目管理员接入 MCP 集成

写表：

- `mcp_servers`
- `mcp_server_authorizations`
- `project_mcp_integrations`
- `activity_logs`
- `audit_logs`

## 5.6 AI 请求执行

写表：

- `ai_usage_events`
- `ai_usage_daily`
- `knowledge_search_logs`
- `tool_invocation_logs`

同步更新：

- `project_token_pools.used_amount`
- `member_ai_quotas.used_amount`
- `platform_credentials.last_used_at`

## 6. 关键约束

1. 个人凭证默认一人一张，`subject_user_id + credential_type=PERSONAL` 唯一。
2. 项目成员必须引用项目角色模板，不能只存裸字符串角色名。
3. 资源级授权必须覆盖知识库、技能、工具、集成四类资产。
4. 项目共享池与成员配额都允许多种配额类型，但每个对象同一配额类型只能有一条生效记录。
5. `knowledge_bases.scope='GLOBAL'` 时 `project_id` 必须为空。
6. `knowledge_bases.scope='PROJECT'` 时 `project_id` 必填。
7. `skills.scope='GLOBAL'` 时 `project_id` 为空，项目是否可用通过 `project_skills` 决定。
8. `tools.scope='PROJECT'` 时必须绑定 `project_id`。
9. `mcp_server_authorizations` 不直接决定可用性，项目是否启用仍由 `project_mcp_integrations` 决定。
10. 所有敏感写操作必须至少写 `activity_logs`，必要时额外写 `audit_logs`。

## 7. MySQL 与向量库边界

MySQL 负责：

1. 结构化元数据
2. 权限与治理配置
3. 用量与审计
4. 工作流和告警

Qdrant 负责：

1. 文档切片向量
2. 检索索引
3. 检索阶段所需元信息

不建议在 MySQL 中保存：

1. 大规模向量数组
2. 完整 chunk 文本冗余
3. 检索缓存结果

## 8. 结论

目标数据库不再以“现有 SQL 有什么表”为起点，而以“原型页面需要什么后端对象”为起点。

正式建库以 `docs/db/ai_platform.sql` 为准，本文作为设计解释和字段责任说明。
