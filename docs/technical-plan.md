# AI 中台 — 全功能技术方案

> 基于原型梳理，对标数据库 60 张表，详细到每个功能点。
> 已实现部分标记 ✅，待开发标记 ⬜。

---

## 一、模块总览

| # | 功能域 | 原型页面 | 涉及表 | 现有后端 |
|---|--------|----------|--------|----------|
| 1 | 用户与组织 | 员工管理 | `users`, `departments` | ⬜ 未开发 |
| 2 | 认证与凭证 | 我的凭证 / 凭证管理 | `platform_credentials`, `key_rotation_logs`, `user_client_bindings`, `client_apps` | ⬜ 未开发 |
| 3 | 项目管理 | 项目空间 / 项目概览 | `projects`, `project_members` | ✅ 基础CRUD |
| 4 | 代码服务 | 代码服务 | `services`, `environments`, `deployments`, `pipeline_runs`, `pipeline_stages` | ✅ 基础CRUD |
| 5 | AI 供应商 | 凭证管理(上游Key) | `ai_providers`, `ai_models`, `provider_api_keys`, `provider_failover_policies` | ⬜ 未开发 |
| 6 | 知识库 | 全局知识库 / 项目知识库 | `knowledge_bases`, `kb_documents`, `knowledge_search_logs`, `project_knowledge_configs` | ⬜ 未开发 |
| 7 | 技能 | 全局技能库 / 项目技能 | `skills`, `skill_feedback`, `project_skills` | ⬜ 未开发 |
| 8 | 工具 | 全局工具集 / 项目工具 | `tool_definitions`, `tool_invocation_logs`, `project_tools` | ⬜ 未开发 |
| 9 | MCP 集成 | 集成市场 / 项目MCP | `mcp_servers`, `oauth_connections`, `project_mcp_integrations`, `service_mcp_configs` | ⬜ 未开发 |
| 10 | 原子能力 | 原子能力中心 | `atomic_capabilities`, `project_atomic_capabilities` | ⬜ 未开发 |
| 11 | 模板 | 代码模板库 | `project_templates` | ⬜ 未开发 |
| 12 | 配额与用量 | 配额管理 / 我的用量 / 用量看板 | `member_ai_quotas`, `ai_usage_events`, `ai_usage_daily`, `project_ai_policies` | ✅ 部分(AI Key配额) |
| 13 | 权限与角色 | 权限管理 / 成员权限 | `roles`, `role_permissions`, `permission_definitions` | ⬜ 未开发 |
| 14 | 告警与通知 | 事故与告警 | `alert_rules`, `alert_events`, `notification_channels`, `incidents` | ⬜ 未开发 |
| 15 | 审计与安全 | 审计安全 | `audit_logs`, `security_events`, `security_rules`, `activity_logs` | ⬜ 未开发 |
| 16 | System Prompt | AI能力配置 | `project_system_prompts` | ⬜ 未开发 |
| 17 | 工作流 | (高级功能) | `workflow_definitions`, `workflow_nodes`, `workflow_executions`, `workflow_execution_steps` | ⬜ 未开发 |
| 18 | 项目管理(敏捷) | (高级功能) | `epics`, `sprints`, `tasks` | ⬜ 未开发 |
| 19 | DORA 指标 | (运营看板) | `dora_metrics` | ⬜ 未开发 |
| 20 | AI 评估 | (运营看板) | `ai_eval_scores` | ⬜ 未开发 |
| 21 | 平台设置 | 设置 | `platform_settings` | ⬜ 未开发 |
| 22 | AI 网关 | (Agent模块) | 复用多表 | ✅ 基础Chat Completion |

---

## 二、各模块详细技术方案

---

### 模块 1: 用户与组织管理

**原型页面**: 员工管理、邀请员工弹窗、编辑员工弹窗、员工详情弹窗

**涉及表**: `users`, `departments`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 1.1 | 员工列表(分页+筛选) | `/api/users` | GET | 支持按部门、角色、状态筛选；分页 |
| 1.2 | 邀请员工 | `/api/users/invite` | POST | 发送邮件邀请，创建 INACTIVE 状态用户 |
| 1.3 | 编辑员工信息 | `/api/users/{id}` | PUT | 修改部门、平台角色、职位等 |
| 1.4 | 员工详情 | `/api/users/{id}` | GET | 返回完整信息：项目列表、凭证状态、角色 |
| 1.5 | 停用/启用账号 | `/api/users/{id}/status` | PATCH | 状态切换 ACTIVE↔DISABLED，同时吊销凭证 |
| 1.6 | 重发邀请 | `/api/users/{id}/reinvite` | POST | 重新发送邮件邀请 |
| 1.7 | 部门列表 | `/api/departments` | GET | 部门下拉选项 |
| 1.8 | 创建部门 | `/api/departments` | POST | 管理员操作 |
| 1.9 | 编辑部门 | `/api/departments/{id}` | PUT | |

**Request/Response DTO**:
- `InviteUserRequest`: email, departmentId, platformRole, projectIds[]
- `UpdateUserRequest`: fullName, departmentId, platformRole, jobTitle, phone
- `UserResponse`: id, email, username, fullName, avatarUrl, department{id,name}, jobTitle, phone, platformRole, status, createdAt
- `UserDetailResponse`: extends UserResponse + projects[], credentials[], clientBindings[]

**业务规则**:
- 邀请时检查 email 唯一性
- 停用账号联动吊销所有 `platform_credentials` (status→REVOKED)
- 平台角色: SUPER_ADMIN / PLATFORM_ADMIN / MEMBER

---

### 模块 2: 认证与凭证管理

**原型页面**: 我的凭证、凭证管理(管理员)、接入与凭证(项目级)

**涉及表**: `platform_credentials`, `key_rotation_logs`, `user_client_bindings`, `client_apps`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 2.1 | 我的凭证列表 | `/api/me/credentials` | GET | 当前用户的所有凭证 |
| 2.2 | 创建个人凭证 | `/api/me/credentials` | POST | 生成 key，返回明文(仅一次) |
| 2.3 | 创建服务账号凭证 | `/api/credentials/service-account` | POST | 绑定项目、IP白名单、有效期 |
| 2.4 | 续期凭证 | `/api/credentials/{id}/renew` | POST | 延长过期时间(30/90/180天) |
| 2.5 | 轮换凭证 | `/api/credentials/{id}/rotate` | POST | 生成新key、旧key 24h宽限期 |
| 2.6 | 吊销凭证 | `/api/credentials/{id}/revoke` | POST | 立即失效，记录原因 |
| 2.7 | 凭证审计列表(管理) | `/api/admin/credentials` | GET | 全平台凭证管理，含过期预警 |
| 2.8 | 轮换日志 | `/api/credentials/{id}/rotation-logs` | GET | 查看轮换历史 |
| 2.9 | 客户端应用列表 | `/api/client-apps` | GET | Claude Code、Cursor等 |
| 2.10 | 用户客户端绑定 | `/api/me/client-bindings` | GET/POST | 绑定/解绑 IDE 工具 |
| 2.11 | IDE接入指南 | `/api/client-apps/{id}/setup` | GET | 返回安装配置指令 |
| 2.12 | 连接验证 | `/api/me/credentials/{id}/verify` | POST | 检查凭证连通性 |

**Request/Response DTO**:
- `CreatePersonalCredentialRequest`: name, expiresInDays
- `CreateServiceAccountCredentialRequest`: name, boundProjectId, expiresInDays, permissionScope{knowledgeBases,skills,tools,mcp}, tokenQuota, ipWhitelist[]
- `CredentialResponse`: id, credentialType, name, keyPrefix, status, expiresAt, lastUsedAt, lastUsedIp, createdAt
- `CredentialCreateResponse`: extends CredentialResponse + rawKey (仅创建时返回)
- `RotateCredentialRequest`: gracePeriodHours (默认24)
- `RenewCredentialRequest`: renewDays (30/90/180)
- `RevokeCredentialRequest`: reason

**业务规则**:
- key 使用 SecureRandom 生成，SHA-256 哈希存储，仅保存前缀用于展示
- 凭证类型: PERSONAL / SERVICE_ACCOUNT / TEMPORARY
- 过期前 7 天发送预警通知
- 轮换时旧 key 有 24h 宽限期(grace period)
- 凭证吊销需记录 `key_rotation_logs`

---

### 模块 3: 项目管理

**原型页面**: 项目空间、新建项目弹窗、项目概览

**涉及表**: `projects`, `project_members`

**现状**: ✅ 已实现基础 CRUD (ProjectController, ProjectMemberController)

#### 待补充功能点

| # | 功能点 | API 端点 | HTTP | 状态 |
|---|--------|----------|------|------|
| 3.1 | 项目列表(分页+搜索) | `/api/projects` | GET | ✅ 已有(需加分页) |
| 3.2 | 创建项目 | `/api/projects` | POST | ✅ 已有(需扩展字段) |
| 3.3 | 项目详情 | `/api/projects/{id}` | GET | ⬜ 需开发 |
| 3.4 | 编辑项目 | `/api/projects/{id}` | PUT | ⬜ 需开发 |
| 3.5 | 归档项目 | `/api/projects/{id}/archive` | POST | ⬜ status→ARCHIVED |
| 3.6 | 项目概览统计 | `/api/projects/{id}/overview` | GET | ⬜ 聚合成员/服务/用量数据 |
| 3.7 | 添加项目成员 | `/api/projects/{id}/members` | POST | ✅ 已有(需对接users表) |
| 3.8 | 成员列表 | `/api/projects/{id}/members` | GET | ✅ 已有 |
| 3.9 | 修改成员角色 | `/api/projects/{id}/members/{mid}` | PUT | ⬜ 需开发 |
| 3.10 | 移除成员 | `/api/projects/{id}/members/{mid}` | DELETE | ⬜ 需开发 |
| 3.11 | 批量修改角色 | `/api/projects/{id}/members/batch-role` | POST | ⬜ 需开发 |
| 3.12 | 成员AI能力分配 | `/api/projects/{id}/members/{mid}/ai-abilities` | PUT | ⬜ 需开发 |

**需重构**: 当前 `project_members` 表已重新设计为 `(project_id, user_id, role)`，需要将现有代码从 name/email 模式迁移到 user_id 关联模式。

---

### 模块 4: 代码服务管理

**原型页面**: 代码服务、添加服务弹窗、服务详情页

**涉及表**: `services`, `environments`, `deployments`, `pipeline_runs`, `pipeline_stages`

**现状**: ✅ 已实现 Service 基础 CRUD

#### 功能点

| # | 功能点 | API 端点 | HTTP | 状态 |
|---|--------|----------|------|------|
| 4.1 | 服务列表 | `/api/projects/{pid}/services` | GET | ✅ 已有 |
| 4.2 | 创建服务 | `/api/projects/{pid}/services` | POST | ✅ 已有(需扩展字段) |
| 4.3 | 服务详情 | `/api/projects/{pid}/services/{sid}` | GET | ⬜ 含环境、部署、管道信息 |
| 4.4 | 编辑服务 | `/api/projects/{pid}/services/{sid}` | PUT | ⬜ |
| 4.5 | 归档服务 | `/api/projects/{pid}/services/{sid}/archive` | POST | ⬜ |
| 4.6 | 环境列表 | `/api/services/{sid}/environments` | GET | ⬜ DEV/STAGING/PROD |
| 4.7 | 创建环境 | `/api/services/{sid}/environments` | POST | ⬜ |
| 4.8 | 编辑环境 | `/api/services/{sid}/environments/{eid}` | PUT | ⬜ |
| 4.9 | 部署历史 | `/api/services/{sid}/deployments` | GET | ⬜ 分页 |
| 4.10 | 触发部署 | `/api/services/{sid}/deployments` | POST | ⬜ |
| 4.11 | 回滚部署 | `/api/services/{sid}/deployments/{did}/rollback` | POST | ⬜ |
| 4.12 | 管道运行列表 | `/api/services/{sid}/pipelines` | GET | ⬜ |
| 4.13 | 管道运行详情(含阶段) | `/api/services/{sid}/pipelines/{rid}` | GET | ⬜ |
| 4.14 | 触发管道 | `/api/services/{sid}/pipelines` | POST | ⬜ |

**DTO**:
- `ServiceDetailResponse`: id, name, description, gitRepoUrl, mainBranch, framework, language, status + environments[], recentDeployments[], latestPipeline
- `CreateEnvironmentRequest`: name, envType, url, deployStrategy, currentBranch
- `DeploymentResponse`: id, version, commitSha, deployUser, changeDescription, status, durationSeconds, deployedAt
- `PipelineRunResponse`: id, pipelineName, branch, commitSha, triggerType, status, stages[], startTime, endTime

---

### 模块 5: AI 供应商管理

**原型页面**: 凭证管理中的"上游API Key"、添加上游API Key弹窗

**涉及表**: `ai_providers`, `ai_models`, `provider_api_keys`, `provider_failover_policies`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 5.1 | 供应商列表 | `/api/admin/providers` | GET | Anthropic/OpenAI/DeepSeek/Google等 |
| 5.2 | 创建供应商 | `/api/admin/providers` | POST | |
| 5.3 | 编辑供应商 | `/api/admin/providers/{id}` | PUT | |
| 5.4 | 模型列表 | `/api/admin/providers/{pid}/models` | GET | 某供应商下的模型列表 |
| 5.5 | 创建模型 | `/api/admin/providers/{pid}/models` | POST | |
| 5.6 | 编辑模型(含定价) | `/api/admin/providers/{pid}/models/{mid}` | PUT | |
| 5.7 | 上游API Key列表 | `/api/admin/provider-keys` | GET | |
| 5.8 | 添加上游API Key | `/api/admin/provider-keys` | POST | 含连通性测试 |
| 5.9 | 编辑Key配置 | `/api/admin/provider-keys/{id}` | PUT | 配额、限速等 |
| 5.10 | 吊销Key | `/api/admin/provider-keys/{id}/revoke` | POST | |
| 5.11 | 测试Key连通性 | `/api/admin/provider-keys/{id}/test` | POST | 发送测试请求验证 |
| 5.12 | 故障转移策略列表 | `/api/admin/failover-policies` | GET | |
| 5.13 | 创建故障转移策略 | `/api/admin/failover-policies` | POST | 主备Key切换规则 |
| 5.14 | 编辑故障转移策略 | `/api/admin/failover-policies/{id}` | PUT | |

**DTO**:
- `CreateProviderRequest`: code, name, providerType, baseUrl
- `ProviderResponse`: id, code, name, providerType, baseUrl, status, modelCount
- `CreateProviderApiKeyRequest`: providerId, label, apiKey, modelsAllowed[], monthlyQuotaTokens, rateLimitRpm, rateLimitTpm, proxyEndpoint
- `ProviderApiKeyResponse`: id, providerId, providerName, label, keyPrefix, modelsAllowed, monthlyQuotaTokens, usedTokensMonth, rateLimitRpm, rateLimitTpm, status
- `CreateFailoverPolicyRequest`: name, primaryKeyId, fallbackKeyId, triggerCondition, triggerThreshold, autoRecovery

**业务规则**:
- API Key 加密存储 (`api_key_encrypted`)，需实现 AES 加解密
- 添加 Key 时先做连通性测试
- 故障转移: ERROR_RATE/LATENCY/QUOTA_EXCEEDED 三种触发条件
- 需与 Agent 模块联动：网关根据 provider_api_keys 路由请求

---

### 模块 6: 知识库管理

**原型页面**: 全局知识库、项目知识库配置、上传文档弹窗、RAG配置弹窗

**涉及表**: `knowledge_bases`, `kb_documents`, `knowledge_search_logs`, `project_knowledge_configs`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 6.1 | 全局知识库列表 | `/api/knowledge-bases?scope=GLOBAL` | GET | |
| 6.2 | 项目知识库列表 | `/api/projects/{pid}/knowledge-bases` | GET | 含全局+项目级 |
| 6.3 | 创建知识库 | `/api/knowledge-bases` | POST | scope=GLOBAL/PROJECT |
| 6.4 | 编辑知识库 | `/api/knowledge-bases/{id}` | PUT | |
| 6.5 | 删除/归档知识库 | `/api/knowledge-bases/{id}/archive` | POST | |
| 6.6 | 文档列表 | `/api/knowledge-bases/{kbId}/documents` | GET | |
| 6.7 | 上传文档 | `/api/knowledge-bases/{kbId}/documents` | POST (multipart) | PDF/MD/Word/TXT/HTML |
| 6.8 | 删除文档 | `/api/knowledge-bases/{kbId}/documents/{did}` | DELETE | |
| 6.9 | 文档处理状态 | `/api/knowledge-bases/{kbId}/documents/{did}` | GET | PENDING→PROCESSING→READY |
| 6.10 | 检索测试 | `/api/knowledge-bases/{kbId}/search` | POST | 输入query返回匹配结果 |
| 6.11 | 检索日志 | `/api/knowledge-bases/{kbId}/search-logs` | GET | |
| 6.12 | RAG 管道配置 | `/api/knowledge-bases/{kbId}/rag-config` | GET/PUT | embedding模型、检索策略 |
| 6.13 | 项目绑定知识库 | `/api/projects/{pid}/knowledge-configs` | POST | 含搜索权重 |
| 6.14 | 项目解绑知识库 | `/api/projects/{pid}/knowledge-configs/{id}` | DELETE | |

**DTO**:
- `CreateKnowledgeBaseRequest`: name, description, scope, projectId, category, embeddingModel, injectMode
- `KnowledgeBaseResponse`: id, name, description, scope, projectId, category, embeddingModel, docCount, totalChunks, hitRate, injectMode, status
- `UploadDocumentRequest`: file (multipart), injectMode
- `KbDocumentResponse`: id, title, fileType, fileSize, chunkCount, hitCount, injectMode, status
- `SearchKnowledgeBaseRequest`: query, searchScope, resultCount
- `SearchResultResponse`: results[]{docId, title, chunk, relevanceScore}, latencyMs
- `RagConfigRequest`: embeddingModel, chunkingStrategy, vectorDb, retrievalMethod, rerankModel, similarityThreshold

**业务规则**:
- 文档上传后异步处理(分块、embedding)
- 支持注入模式: AUTO_INJECT(自动注入上下文) / ON_DEMAND(按需检索)
- 检索方式: 纯语义 / 混合BM25 / 关键词
- 命中率统计

---

### 模块 7: 技能管理

**原型页面**: 全局技能库、新建技能弹窗、技能审核弹窗、项目技能配置

**涉及表**: `skills`, `skill_feedback`, `project_skills`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 7.1 | 全局技能列表 | `/api/skills?scope=GLOBAL` | GET | 分页+分类筛选 |
| 7.2 | 项目技能列表 | `/api/projects/{pid}/skills` | GET | |
| 7.3 | 创建技能 | `/api/skills` | POST | |
| 7.4 | 编辑技能 | `/api/skills/{id}` | PUT | |
| 7.5 | 技能详情 | `/api/skills/{id}` | GET | 含使用统计 |
| 7.6 | 发布技能(提交审核) | `/api/skills/{id}/publish` | POST | DRAFT→审核中 |
| 7.7 | 审核技能 | `/api/skills/{id}/review` | POST | 通过→PUBLISHED / 拒绝→DRAFT |
| 7.8 | 废弃技能 | `/api/skills/{id}/deprecate` | POST | PUBLISHED→DEPRECATED |
| 7.9 | 技能反馈(点赞/踩) | `/api/skills/{id}/feedback` | POST | UP/DOWN |
| 7.10 | 技能反馈列表 | `/api/skills/{id}/feedback` | GET | |
| 7.11 | 项目启用技能 | `/api/projects/{pid}/skills` | POST | skillId绑定 |
| 7.12 | 项目禁用技能 | `/api/projects/{pid}/skills/{id}` | DELETE | |

**DTO**:
- `CreateSkillRequest`: skillKey, name, description, scope, projectId, category, systemPrompt, knowledgeRefs[], boundTools[], parameters[], slashCommand
- `SkillResponse`: id, skillKey, name, description, scope, category, systemPrompt, knowledgeRefs, boundTools, parameters, slashCommand, version, status, usageCount, satisfactionUp, satisfactionDown
- `ReviewSkillRequest`: approved (boolean), comment
- `SkillFeedbackRequest`: rating (UP/DOWN), comment, usageEventId

**业务规则**:
- 技能生命周期: DRAFT → PUBLISHED → DEPRECATED
- System Prompt 支持 `{{variable}}` 模板语法
- knowledgeRefs: 关联知识库 ID 列表(JSON)
- boundTools: 关联工具 ID 列表(JSON)
- 审核流程: 需 PLATFORM_ADMIN 角色审批

---

### 模块 8: 工具管理

**原型页面**: 全局工具集、注册工具弹窗、项目工具配置

**涉及表**: `tool_definitions`, `tool_invocation_logs`, `project_tools`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 8.1 | 全局工具列表 | `/api/tools?scope=GLOBAL` | GET | |
| 8.2 | 项目工具列表 | `/api/projects/{pid}/tools` | GET | |
| 8.3 | 注册工具 | `/api/tools` | POST | |
| 8.4 | 编辑工具 | `/api/tools/{id}` | PUT | |
| 8.5 | 工具详情 | `/api/tools/{id}` | GET | |
| 8.6 | 禁用工具 | `/api/tools/{id}/disable` | POST | |
| 8.7 | 测试工具 | `/api/tools/{id}/test` | POST | 发送测试请求 |
| 8.8 | 工具调用日志 | `/api/tools/{id}/invocation-logs` | GET | |
| 8.9 | 项目启用工具 | `/api/projects/{pid}/tools` | POST | |
| 8.10 | 项目禁用工具 | `/api/projects/{pid}/tools/{id}` | DELETE | |

**DTO**:
- `CreateToolRequest`: toolName, displayName, description, scope, projectId, category, inputSchema(JSON), outputSchema(JSON), implType(INTERNAL/HTTP_CALLBACK/MCP_PROXY), implConfig(JSON), permissionRequired, auditLevel
- `ToolResponse`: id, toolName, displayName, description, scope, category, inputSchema, outputSchema, implType, permissionRequired, auditLevel, status
- `ToolInvocationLogResponse`: id, toolId, userId, skillId, inputData, outputData, errorMessage, durationMs, status, executedAt

**业务规则**:
- 工具实现类型: INTERNAL(内置) / HTTP_CALLBACK(外部HTTP) / MCP_PROXY(MCP代理)
- 审计级别: NORMAL / SENSITIVE / CRITICAL
- SENSITIVE/CRITICAL 级别工具调用需记录详细日志

---

### 模块 9: MCP 集成管理

**原型页面**: 集成市场、创建集成弹窗、项目MCP集成

**涉及表**: `mcp_servers`, `oauth_connections`, `project_mcp_integrations`, `service_mcp_configs`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 9.1 | MCP Server 列表 | `/api/mcp-servers` | GET | 按类型筛选 |
| 9.2 | 注册 MCP Server | `/api/mcp-servers` | POST | |
| 9.3 | 编辑 MCP Server | `/api/mcp-servers/{id}` | PUT | |
| 9.4 | 删除 MCP Server | `/api/mcp-servers/{id}` | DELETE | |
| 9.5 | 测试 MCP 连通性 | `/api/mcp-servers/{id}/test` | POST | |
| 9.6 | MCP 工具发现 | `/api/mcp-servers/{id}/discover-tools` | POST | 自动发现MCP暴露的工具 |
| 9.7 | OAuth 连接管理 | `/api/mcp-servers/{id}/oauth` | GET/POST/DELETE | |
| 9.8 | 项目绑定MCP | `/api/projects/{pid}/mcp-integrations` | POST | |
| 9.9 | 项目MCP集成列表 | `/api/projects/{pid}/mcp-integrations` | GET | |
| 9.10 | 项目解绑MCP | `/api/projects/{pid}/mcp-integrations/{id}` | DELETE | |
| 9.11 | 服务MCP配置 | `/api/services/{sid}/mcp-config` | GET/PUT | 暴露工具、资源配置 |

**DTO**:
- `CreateMcpServerRequest`: serverName, displayName, description, serverType, projectId, category, serverUrl, authType, authConfig(JSON), capabilities(JSON)
- `McpServerResponse`: id, serverName, displayName, description, serverType, category, serverUrl, authType, capabilities, status, lastCheckedAt
- `OAuthConnectionResponse`: id, providerName, accountName, scopes, status, connectedAt

**业务规则**:
- MCP Server 类型: BUILTIN / OFFICIAL / ENTERPRISE / PROJECT
- 传输协议: Streamable HTTP / SSE / stdio
- 认证方式: NONE / BEARER / OAUTH2 / API_KEY
- 自动发现: 调用 MCP Server 的 tools/list 端点

---

### 模块 10: 原子能力中心

**原型页面**: 原子能力中心、发布原子能力弹窗

**涉及表**: `atomic_capabilities`, `project_atomic_capabilities`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 10.1 | 能力列表 | `/api/atomic-capabilities` | GET | 分类筛选 |
| 10.2 | 发布能力 | `/api/atomic-capabilities` | POST | |
| 10.3 | 编辑能力 | `/api/atomic-capabilities/{id}` | PUT | |
| 10.4 | 能力详情 | `/api/atomic-capabilities/{id}` | GET | 含文档、API规范 |
| 10.5 | 废弃能力 | `/api/atomic-capabilities/{id}/deprecate` | POST | |
| 10.6 | 项目订阅能力 | `/api/projects/{pid}/atomic-capabilities` | POST | |
| 10.7 | 项目取消订阅 | `/api/projects/{pid}/atomic-capabilities/{id}` | DELETE | |
| 10.8 | 项目已订阅列表 | `/api/projects/{pid}/atomic-capabilities` | GET | |

**DTO**:
- `CreateAtomicCapabilityRequest`: name, code, description, icon, category, docContent, apiSpecUrl, gitRepoUrl, version, supportedLanguages
- `AtomicCapabilityResponse`: id, name, code, description, icon, category, docContent, apiSpecUrl, gitRepoUrl, version, supportedLanguages, subscriptionCount, status

---

### 模块 11: 代码模板库

**原型页面**: 代码模板库、模板详情弹窗、上传模板弹窗

**涉及表**: `project_templates`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 11.1 | 模板列表 | `/api/templates` | GET | 按类型、语言、框架筛选 |
| 11.2 | 模板详情 | `/api/templates/{id}` | GET | 含目录结构、版本历史 |
| 11.3 | 上传模板 | `/api/templates` | POST | Git导入或ZIP上传 |
| 11.4 | 编辑模板 | `/api/templates/{id}` | PUT | |
| 11.5 | 归档模板 | `/api/templates/{id}/archive` | POST | |
| 11.6 | 从模板创建服务 | `/api/projects/{pid}/services/from-template` | POST | 克隆模板到新仓库 |

**DTO**:
- `CreateTemplateRequest`: name, description, templateType, scope, projectId, language, framework, templateContent(或gitUrl)
- `TemplateResponse`: id, name, description, templateType, scope, language, framework, downloadCount, status, createdAt

---

### 模块 12: 配额与用量管理

**原型页面**: 配额管理、我的用量、用量看板(管理)

**涉及表**: `member_ai_quotas`, `ai_usage_events`, `ai_usage_daily`, `project_ai_policies`

**现状**: ✅ 部分实现(AI Key 级别的配额)

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 12.1 | 我的用量概览 | `/api/me/usage` | GET | Token消耗趋势 |
| 12.2 | 我的用量明细 | `/api/me/usage/events` | GET | 分页，按时间/模型筛选 |
| 12.3 | 项目配额管理 | `/api/projects/{pid}/quotas` | GET | |
| 12.4 | 设置成员配额 | `/api/projects/{pid}/members/{mid}/quota` | PUT | |
| 12.5 | 成员配额列表 | `/api/projects/{pid}/member-quotas` | GET | |
| 12.6 | 平台用量看板 | `/api/admin/usage/dashboard` | GET | 全平台聚合 |
| 12.7 | 项目用量统计 | `/api/projects/{pid}/usage/summary` | GET | 按天/周/月 |
| 12.8 | 用量按模型分布 | `/api/projects/{pid}/usage/by-model` | GET | 饼图数据 |
| 12.9 | 用量按成员分布 | `/api/projects/{pid}/usage/by-member` | GET | |
| 12.10 | 项目AI策略列表 | `/api/projects/{pid}/ai-policies` | GET | |
| 12.11 | 创建AI策略 | `/api/projects/{pid}/ai-policies` | POST | 配额限制、成本限制等 |
| 12.12 | 编辑AI策略 | `/api/projects/{pid}/ai-policies/{id}` | PUT | |
| 12.13 | 用量日报聚合(定时任务) | — (Scheduled Job) | — | ai_usage_events → ai_usage_daily |

**DTO**:
- `UsageSummaryResponse`: totalRequests, totalTokens, totalCost, period, trend[]
- `UsageEventResponse`: id, credentialId, userId, provider, model, requestMode, inputTokens, outputTokens, totalTokens, costAmount, status, latencyMs, occurredAt
- `MemberQuotaResponse`: userId, userName, quotaType, quotaLimit, usedAmount, resetCycle, status
- `SetMemberQuotaRequest`: quotaType(TOKEN_QUOTA/COST_QUOTA/REQUEST_QUOTA), quotaLimit, resetCycle
- `CreateAiPolicyRequest`: policyType, ruleContent(JSON), priority

**业务规则**:
- 配额类型: TOKEN_QUOTA / COST_QUOTA / REQUEST_QUOTA
- 重置周期: DAILY / WEEKLY / MONTHLY
- 超额策略: BLOCK / WARN / DOWNGRADE_MODEL
- 需定时任务将 `ai_usage_events` 聚合到 `ai_usage_daily`
- 网关层实时检查配额

---

### 模块 13: 权限与角色管理

**原型页面**: 权限管理、编辑角色弹窗、编辑权限点弹窗

**涉及表**: `roles`, `role_permissions`, `permission_definitions`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 13.1 | 平台角色列表 | `/api/admin/roles?scope=PLATFORM` | GET | |
| 13.2 | 项目角色模板列表 | `/api/admin/roles?scope=PROJECT` | GET | |
| 13.3 | 创建角色 | `/api/admin/roles` | POST | |
| 13.4 | 编辑角色 | `/api/admin/roles/{id}` | PUT | |
| 13.5 | 角色权限配置 | `/api/admin/roles/{id}/permissions` | GET/PUT | RBAC矩阵 |
| 13.6 | 权限点列表 | `/api/admin/permissions` | GET | 按模块分组 |
| 13.7 | 创建权限点 | `/api/admin/permissions` | POST | |
| 13.8 | 编辑权限点 | `/api/admin/permissions/{id}` | PUT | |
| 13.9 | 权限校验 | `/api/auth/check-permission` | POST | 检查user+resource+action |

**DTO**:
- `RoleResponse`: id, name, code, roleScope, description, isSystem, defaultQuotaTokens, status, permissions[]
- `CreateRoleRequest`: name, code, roleScope, description, defaultQuotaTokens
- `PermissionDefinitionResponse`: id, module, permissionKey, name, description, permissionScope
- `RolePermissionRequest`: permissions[]{permissionId, accessLevel}

**业务规则**:
- 平台角色: SUPER_ADMIN / PLATFORM_ADMIN / MEMBER
- 项目角色: ADMIN / MEMBER / VIEWER (可自定义)
- 权限访问级别: NONE / VIEW / CALL / CREATE / FULL_CONTROL
- 系统内置角色 (is_system=1) 不可删除
- 权限校验需在 Controller 层通过注解实现 (如 @RequirePermission)

---

### 模块 14: 告警与事故管理

**原型页面**: 事故与告警、告警规则配置弹窗

**涉及表**: `alert_rules`, `alert_events`, `notification_channels`, `incidents`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 14.1 | 告警规则列表 | `/api/alert-rules` | GET | 按范围(平台/项目)筛选 |
| 14.2 | 创建告警规则 | `/api/alert-rules` | POST | |
| 14.3 | 编辑告警规则 | `/api/alert-rules/{id}` | PUT | |
| 14.4 | 启用/禁用规则 | `/api/alert-rules/{id}/status` | PATCH | |
| 14.5 | 告警事件列表 | `/api/alert-events` | GET | 分页，按状态/严重度筛选 |
| 14.6 | 确认告警 | `/api/alert-events/{id}/acknowledge` | POST | FIRING→ACKNOWLEDGED |
| 14.7 | 解决告警 | `/api/alert-events/{id}/resolve` | POST | →RESOLVED |
| 14.8 | 事故列表 | `/api/projects/{pid}/incidents` | GET | |
| 14.9 | 创建事故 | `/api/projects/{pid}/incidents` | POST | |
| 14.10 | 事故详情 | `/api/projects/{pid}/incidents/{id}` | GET | 含AI诊断 |
| 14.11 | 更新事故状态 | `/api/projects/{pid}/incidents/{id}/status` | PATCH | |
| 14.12 | AI诊断事故 | `/api/projects/{pid}/incidents/{id}/ai-diagnose` | POST | 调用AI分析错误栈 |
| 14.13 | 通知渠道列表 | `/api/admin/notification-channels` | GET | |
| 14.14 | 创建通知渠道 | `/api/admin/notification-channels` | POST | email/企微/钉钉/Slack/Webhook/SMS |
| 14.15 | 编辑通知渠道 | `/api/admin/notification-channels/{id}` | PUT | |
| 14.16 | 测试通知渠道 | `/api/admin/notification-channels/{id}/test` | POST | 发送测试消息 |

**DTO**:
- `CreateAlertRuleRequest`: name, description, triggerCondition, triggerExpression(JSON), severity, notificationChannelIds[], cooldownMinutes, scope, projectId
- `AlertEventResponse`: id, ruleId, ruleName, projectId, userId, triggerValue, message, notifiedChannels, severity, status, createdAt, resolvedAt
- `IncidentResponse`: id, projectId, serviceId, title, severity, status, errorStack, errorRequest, aiDiagnosis, aiDiagnosisStatus, assigneeUserId, githubIssueUrl, resolvedAt
- `CreateNotificationChannelRequest`: name, channelType, config(JSON), isDefault

**业务规则**:
- 告警触发条件: Token使用≥90%、API错误率、P99延迟、连续失败
- 冷却时间: 防止重复告警
- 严重度: CRITICAL / HIGH / MEDIUM / LOW
- 事故AI诊断: 将 errorStack 发送给 AI 分析根因
- 通知渠道需支持模板化消息

---

### 模块 15: 审计与安全

**原型页面**: 审计安全

**涉及表**: `audit_logs`, `security_events`, `security_rules`, `activity_logs`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 15.1 | 审计日志列表 | `/api/admin/audit-logs` | GET | 分页+筛选(用户/操作/时间) |
| 15.2 | 审计日志详情 | `/api/admin/audit-logs/{id}` | GET | |
| 15.3 | 安全事件列表 | `/api/admin/security-events` | GET | |
| 15.4 | 安全事件详情 | `/api/admin/security-events/{id}` | GET | 含AI分析 |
| 15.5 | 处理安全事件 | `/api/admin/security-events/{id}/status` | PATCH | |
| 15.6 | 安全规则列表 | `/api/admin/security-rules` | GET | |
| 15.7 | 创建安全规则 | `/api/admin/security-rules` | POST | IP白/黑名单、限流等 |
| 15.8 | 编辑安全规则 | `/api/admin/security-rules/{id}` | PUT | |
| 15.9 | 活动日志(项目级) | `/api/projects/{pid}/activity-logs` | GET | |

**DTO**:
- `AuditLogResponse`: id, userId, userName, projectId, action, targetType, targetId, detail, result, ipAddress, userAgent, occurredAt
- `SecurityEventResponse`: id, projectId, userId, eventType, severity, description, aiAnalysis, actionTaken, status
- `CreateSecurityRuleRequest`: ruleName, ruleType, ruleExpression, description, priority
- `ActivityLogResponse`: id, projectId, userId, actorName, actionType, summary, targetType, targetId, targetName, occurredAt

**业务规则**:
- 审计日志通过 AOP 切面自动记录
- 安全事件类型: 认证失败、授权失败、恶意活动、可疑模式、API滥用、凭证泄露
- 安全规则类型: IP白名单/黑名单、限流、内容过滤、认证策略、加密策略、审计策略
- 需实现 `@AuditLog` 注解自动记录操作

---

### 模块 16: System Prompt 管理

**原型页面**: AI能力配置中的 System Prompt 部分

**涉及表**: `project_system_prompts`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 16.1 | 项目Prompt列表 | `/api/projects/{pid}/system-prompts` | GET | |
| 16.2 | 创建Prompt | `/api/projects/{pid}/system-prompts` | POST | |
| 16.3 | 编辑Prompt | `/api/projects/{pid}/system-prompts/{id}` | PUT | |
| 16.4 | 删除Prompt | `/api/projects/{pid}/system-prompts/{id}` | DELETE | |
| 16.5 | 调整Prompt优先级 | `/api/projects/{pid}/system-prompts/{id}/priority` | PATCH | |

**DTO**:
- `CreateSystemPromptRequest`: promptName, promptType(GLOBAL_INJECT/PROJECT_CONTEXT/CODING_STANDARD/SECURITY_RULES/CUSTOM), content, injectStrategy(ALWAYS/ON_DEMAND/DISABLED), maxTokens, priority
- `SystemPromptResponse`: id, projectId, promptName, promptType, content, injectStrategy, maxTokens, priority, status

**业务规则**:
- 注入策略: ALWAYS(每次请求注入) / ON_DEMAND(按需) / DISABLED
- 按 priority 排序注入
- maxTokens 限制单个 prompt 的 token 消耗
- 网关层: 在请求发往上游前注入 system prompt

---

### 模块 17: 工作流引擎 (高级)

**涉及表**: `workflow_definitions`, `workflow_nodes`, `workflow_executions`, `workflow_execution_steps`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 17.1 | 工作流定义列表 | `/api/projects/{pid}/workflows` | GET | |
| 17.2 | 创建工作流 | `/api/projects/{pid}/workflows` | POST | |
| 17.3 | 编辑工作流(画布) | `/api/projects/{pid}/workflows/{id}` | PUT | JSON定义 |
| 17.4 | 发布工作流 | `/api/projects/{pid}/workflows/{id}/publish` | POST | |
| 17.5 | 归档工作流 | `/api/projects/{pid}/workflows/{id}/archive` | POST | |
| 17.6 | 工作流节点管理 | `/api/workflows/{wid}/nodes` | GET/POST/PUT/DELETE | |
| 17.7 | 手动触发执行 | `/api/workflows/{wid}/execute` | POST | |
| 17.8 | 执行列表 | `/api/workflows/{wid}/executions` | GET | |
| 17.9 | 执行详情(含步骤) | `/api/workflows/{wid}/executions/{eid}` | GET | |
| 17.10 | 中止执行 | `/api/workflows/{wid}/executions/{eid}/abort` | POST | |

---

### 模块 18: 敏捷项目管理 (高级)

**涉及表**: `epics`, `sprints`, `tasks`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 18.1 | Epic CRUD | `/api/projects/{pid}/epics` | GET/POST/PUT | |
| 18.2 | Sprint CRUD | `/api/projects/{pid}/sprints` | GET/POST/PUT | |
| 18.3 | Sprint 激活/完成 | `/api/projects/{pid}/sprints/{id}/status` | PATCH | |
| 18.4 | Task CRUD | `/api/projects/{pid}/tasks` | GET/POST/PUT/DELETE | |
| 18.5 | Task 看板(按状态) | `/api/projects/{pid}/tasks?groupBy=status` | GET | |
| 18.6 | 指派任务 | `/api/projects/{pid}/tasks/{id}/assign` | PATCH | |

---

### 模块 19: DORA 指标

**涉及表**: `dora_metrics`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 19.1 | DORA指标查询 | `/api/projects/{pid}/dora-metrics` | GET | 按周期筛选 |
| 19.2 | DORA指标计算(定时) | — (Scheduled Job) | — | 从deployments/incidents聚合 |
| 19.3 | DORA指标看板 | `/api/admin/dora-dashboard` | GET | 全平台汇总 |

---

### 模块 20: AI 评估

**涉及表**: `ai_eval_scores`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 20.1 | 评估结果列表 | `/api/admin/ai-eval-scores` | GET | |
| 20.2 | 评估详情 | `/api/admin/ai-eval-scores/{id}` | GET | |
| 20.3 | 触发评估(定时) | — (Scheduled Job) | — | 基于usage数据评分 |

---

### 模块 21: 平台设置

**涉及表**: `platform_settings`

#### 功能点

| # | 功能点 | API 端点 | HTTP | 说明 |
|---|--------|----------|------|------|
| 21.1 | 获取所有设置 | `/api/admin/settings` | GET | |
| 21.2 | 更新设置 | `/api/admin/settings/{key}` | PUT | |
| 21.3 | 批量更新 | `/api/admin/settings` | PUT | |

---

### 模块 22: AI 网关 (Agent 模块)

**现状**: ✅ 已实现基础 Chat Completion 代理

**涉及**: agent 模块，复用多表

#### 待补充功能点

| # | 功能点 | 说明 | 状态 |
|---|--------|------|------|
| 22.1 | Chat Completion 代理 | 基础请求转发 | ✅ 已实现 |
| 22.2 | Streaming 支持 | SSE流式响应 | ⬜ 需完善 |
| 22.3 | 多供应商路由 | 根据 provider_api_keys 路由 | ⬜ 需扩展 |
| 22.4 | 故障转移 | 主Key失败自动切备Key | ⬜ |
| 22.5 | System Prompt 注入 | 请求前注入项目级 prompt | ⬜ |
| 22.6 | 知识库 RAG 增强 | 自动检索知识库增强上下文 | ⬜ |
| 22.7 | 用量事件记录 | 写入 ai_usage_events | ⬜ |
| 22.8 | 成员级配额检查 | 检查 member_ai_quotas | ⬜ |
| 22.9 | 限流 | RPM/TPM 限制 | ⬜ |
| 22.10 | 内容安全过滤 | 检查 security_rules | ⬜ |
| 22.11 | MCP Tool 调用代理 | 代理调用 MCP Server 工具 | ⬜ |
| 22.12 | Embedding 代理 | 代理 embedding 请求 | ⬜ |

---

## 三、横切关注点

### 3.1 认证与鉴权框架

```
请求 → Gateway → Token解析 → 用户身份识别 → 权限校验 → Controller
```

- **平台Web端**: 基于 Session/JWT 认证 (users 表)
- **API 端**: 基于 platform_credentials 的 Bearer Token
- **AI 网关**: 基于 platform_access_tokens 的 Bearer Token (已实现)
- **权限校验**: `@RequirePermission(module="project", action="CREATE")` 注解 + AOP

### 3.2 审计日志框架

```java
@AuditLog(action="CREATE_PROJECT", targetType="PROJECT")
public ProjectResponse create(@RequestBody CreateProjectRequest req) { ... }
```

- 通过 AOP 切面自动记录到 `audit_logs`
- 记录: userId, action, targetType, targetId, detail(JSON), result, ip, userAgent

### 3.3 分页与排序

统一分页方案:
```
GET /api/xxx?page=1&size=20&sort=createdAt,desc
```
- 使用 MyBatis Plus 的 `Page<T>` 分页
- 返回格式: `{ items: [], total: N, page: N, size: N }`

### 3.4 异常处理

已有部分 `@ResponseStatus` 异常类，需统一为:
```json
{
  "code": "PROJECT_NOT_FOUND",
  "message": "项目不存在",
  "timestamp": "2026-03-20T10:00:00Z"
}
```

### 3.5 定时任务

| 任务 | 频率 | 说明 |
|------|------|------|
| 用量日报聚合 | 每日 02:00 | ai_usage_events → ai_usage_daily |
| 配额重置 | 每日/周/月 | 重置 member_ai_quotas.used_amount |
| 凭证过期预警 | 每日 09:00 | 扫描即将过期凭证发送通知 |
| DORA 指标计算 | 每周一 | 从 deployments/incidents 聚合 |
| AI 评估 | 每月 1 日 | 基于 usage 数据评分 |
| MCP 健康检查 | 每 5 分钟 | 检查 MCP Server 连通性 |

### 3.6 事件驱动

建议引入 Spring Event 机制:
- `CredentialRevokedEvent` → 网关缓存失效
- `QuotaExceededEvent` → 触发告警
- `SecurityIncidentEvent` → 记录安全事件
- `DeploymentCompletedEvent` → 更新环境状态

---

## 四、开发优先级建议

### P0 — 核心链路 (先完成这些才能跑通主流程)

1. **模块 1**: 用户与组织 — 所有模块依赖 users 表
2. **模块 5**: AI 供应商管理 — 网关路由依赖
3. **模块 13**: 权限与角色 — 鉴权基础设施
4. **模块 2**: 凭证管理 — API 接入依赖
5. **模块 22**: 网关增强(多供应商、用量记录)

### P1 — 核心功能

6. **模块 3**: 项目管理(补全 CRUD)
7. **模块 12**: 配额与用量
8. **模块 6**: 知识库管理
9. **模块 7**: 技能管理
10. **模块 8**: 工具管理

### P2 — 增强功能

11. **模块 9**: MCP 集成
12. **模块 14**: 告警与事故
13. **模块 15**: 审计与安全
14. **模块 16**: System Prompt
15. **模块 4**: 服务管理(补全部署/管道)

### P3 — 高级功能

16. **模块 10**: 原子能力
17. **模块 11**: 模板库
18. **模块 17**: 工作流
19. **模块 18**: 敏捷管理
20. **模块 19**: DORA 指标
21. **模块 20**: AI 评估
22. **模块 21**: 平台设置

---

## 五、现有代码重构项

| 项目 | 说明 | 优先级 |
|------|------|--------|
| project_members 表结构变更 | 现有代码用 name/email，新表用 user_id 关联 | P0 |
| 统一分页 | 现有接口无分页，需加入 | P0 |
| provider_api_keys 替换 project_ai_keys | 现有 project_ai_keys 表在新 schema 中不存在，需迁移到 provider_api_keys | P0 |
| 统一异常响应格式 | 现有 @ResponseStatus 需包装为标准 JSON | P1 |
| 引入 Spring Security | 现有无认证框架 | P0 |
| 引入 AOP 审计 | 自动记录操作日志 | P1 |
| Agent 模块对接新表 | 现有 Ref 实体需对齐新表结构 | P0 |
