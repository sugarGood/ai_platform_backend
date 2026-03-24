# AI 平台后端接口清单

## 1. 设计原则

接口设计以“页面动作可落地”为目标，不以“当前代码里已有多少 Controller”为目标。

组织方式：

1. 先按页面和业务动作分组。
2. 再列出对应接口。
3. 明确每个动作的核心读写对象和副作用。

## 2. 接口分层

## 2.1 控制面接口

由管理后台和项目配置页面调用，负责配置、查询、治理。

## 2.2 运行面接口

由 Claude Code、Cursor、Codex、自建 Agent 调用，负责承载 AI 请求、上下文注入、MCP 能力暴露。

## 2.3 内部接口

供运行面网关调用控制面，如凭证验证、项目装配、配额校验、能力清单加载。

## 3. 个人工作台

## 3.1 我的凭证

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/me/credential` | 获取当前成员的个人凭证、默认项目、可访问项目范围 |
| `POST` | `/api/me/credential/rotate` | 轮换个人凭证 |
| `POST` | `/api/me/credential/renew` | 续签个人凭证 |
| `POST` | `/api/me/credential/test` | 测试凭证连通性 |
| `PUT` | `/api/me/current-project` | 切换当前工作项目 |
| `GET` | `/api/me/setup-materials` | 获取按客户端聚合的接入材料 |
| `POST` | `/api/me/setup-script` | 生成一键安装脚本/链接 |

关键返回字段：

- `credentialCode`
- `keyPrefix`
- `credentialType`
- `status`
- `expiresAt`
- `defaultProject`
- `accessibleProjects`
- `setupMaterials`

副作用：

- 轮换和续签写 `credential_rotation_logs`
- 切换项目更新 `platform_credentials.default_project_id`
- 测试连通性更新 `user_client_bindings.last_active_at`

## 3.2 我的能力

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/me/abilities` | 获取当前成员在当前项目下的知识库/技能/工具/集成清单 |
| `GET` | `/api/me/abilities/projects` | 获取各项目的可用能力概览 |

关键返回结构：

- `knowledgeBases`
- `skills`
- `tools`
- `integrations`
- `inheritanceSummary`

## 3.3 我的用量

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/me/usage/summary` | 获取本月个人用量总览 |
| `GET` | `/api/me/usage/trend` | 获取个人用量趋势 |
| `GET` | `/api/me/usage/events` | 获取请求级明细 |
| `GET` | `/api/me/usage/projects` | 获取按项目分布 |

## 4. 项目空间

## 4.1 项目列表与建项

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects` | 获取项目列表 |
| `POST` | `/api/projects` | 创建项目 |
| `GET` | `/api/projects/{projectId}` | 获取项目详情 |
| `PUT` | `/api/projects/{projectId}` | 更新项目基础信息 |
| `POST` | `/api/projects/{projectId}/archive` | 归档项目 |
| `GET` | `/api/projects/{projectId}/overview` | 获取项目概览卡片数据 |

建项请求体建议字段：

- `name`
- `code`
- `description`
- `icon`
- `projectType`
- `ownerUserId`
- `templateId`
- `initialKnowledgeBaseIds`
- `initialSkillIds`
- `initialRoleTemplateSet`

建项副作用：

1. 初始化默认角色模板。
2. 初始化项目共享 Token 池。
3. 可选复制模板、知识库和技能。
4. 写 `activity_logs`。

## 4.2 项目接入与凭证

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/workspace` | 获取项目接入页数据 |
| `GET` | `/api/projects/{projectId}/workspace/mcp-endpoints` | 获取项目 MCP 端点与全局端点 |
| `GET` | `/api/projects/{projectId}/workspace/member-status` | 获取项目成员接入状态 |
| `POST` | `/api/projects/{projectId}/workspace/send-guides` | 批量发送接入指南 |
| `POST` | `/api/projects/{projectId}/workspace/check-members` | 批量检测成员接入状态 |
| `POST` | `/api/projects/{projectId}/service-credentials` | 创建项目服务账号凭证 |
| `POST` | `/api/projects/{projectId}/temporary-credentials` | 创建项目临时凭证 |

## 5. 项目成员与权限

## 5.1 成员管理

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/members` | 获取项目成员列表 |
| `POST` | `/api/projects/{projectId}/members` | 添加成员 |
| `DELETE` | `/api/projects/{projectId}/members/{memberId}` | 移除成员 |
| `PUT` | `/api/projects/{projectId}/members/{memberId}/role` | 修改成员角色模板 |
| `PUT` | `/api/projects/{projectId}/members/{memberId}/permissions` | 保存成员权限覆写 |
| `PUT` | `/api/projects/{projectId}/members/{memberId}/resource-grants` | 保存成员资源授权范围 |

关键请求体：

- `projectRoleTemplateId`
- `resetAbilitiesToRoleDefault`
- `moduleOverrides`
- `knowledgeBaseIds`
- `skillIds`
- `toolIds`
- `integrationIds`
- `memberQuota`

## 5.2 项目角色模板

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/role-templates` | 获取项目角色模板列表 |
| `POST` | `/api/projects/{projectId}/role-templates` | 新建项目角色模板 |
| `PUT` | `/api/projects/{projectId}/role-templates/{roleTemplateId}` | 编辑项目角色模板 |
| `PUT` | `/api/projects/{projectId}/role-templates/{roleTemplateId}/permissions` | 保存模块权限矩阵 |
| `POST` | `/api/projects/{projectId}/role-templates/reset-defaults` | 重置为平台默认模板 |
| `GET` | `/api/projects/{projectId}/permission-matrix` | 获取项目权限矩阵 |
| `GET` | `/api/projects/{projectId}/permission-logs` | 获取权限变更日志 |

## 6. 项目 AI 能力配置

## 6.1 知识库

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/ai/knowledge` | 获取项目 AI 能力页中的知识库面板数据 |
| `POST` | `/api/knowledge-bases` | 创建知识库 |
| `PUT` | `/api/knowledge-bases/{knowledgeBaseId}` | 编辑知识库 |
| `POST` | `/api/knowledge-bases/{knowledgeBaseId}/documents` | 上传文档 |
| `DELETE` | `/api/knowledge-bases/{knowledgeBaseId}/documents/{documentId}` | 删除文档 |
| `POST` | `/api/knowledge-bases/{knowledgeBaseId}/search-test` | 检索测试 |
| `PUT` | `/api/projects/{projectId}/knowledge-configs/{configId}` | 保存继承和检索权重配置 |
| `POST` | `/api/projects/{projectId}/knowledge-configs` | 绑定知识库到项目 |
| `DELETE` | `/api/projects/{projectId}/knowledge-configs/{configId}` | 移除知识库绑定 |

## 6.2 技能

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/ai/skills` | 获取技能面板数据 |
| `POST` | `/api/skills` | 创建技能 |
| `PUT` | `/api/skills/{skillId}` | 编辑技能 |
| `POST` | `/api/skills/{skillId}/test` | 测试技能 |
| `POST` | `/api/skills/{skillId}/publish` | 发布技能 |
| `POST` | `/api/skills/{skillId}/clone-to-project` | 克隆全局技能到项目 |
| `POST` | `/api/projects/{projectId}/skills` | 启用技能 |
| `DELETE` | `/api/projects/{projectId}/skills/{skillId}` | 移除技能 |
| `POST` | `/api/skills/{skillId}/feedback` | 提交技能反馈 |

## 6.3 工具

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/ai/tools` | 获取工具面板数据 |
| `POST` | `/api/tools` | 注册工具 |
| `PUT` | `/api/tools/{toolId}` | 编辑工具 |
| `PUT` | `/api/tools/{toolId}/status` | 启停工具 |
| `POST` | `/api/tools/{toolId}/test` | 测试工具 |
| `GET` | `/api/tools/{toolId}/invocation-logs` | 获取工具调用日志 |
| `POST` | `/api/projects/{projectId}/tools` | 启用工具到项目 |
| `DELETE` | `/api/projects/{projectId}/tools/{toolId}` | 从项目移除工具 |

## 6.4 System Prompt

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/system-prompts` | 获取项目 Prompt 列表 |
| `POST` | `/api/projects/{projectId}/system-prompts` | 新建 Prompt |
| `PUT` | `/api/projects/{projectId}/system-prompts/{promptId}` | 编辑 Prompt |
| `PATCH` | `/api/projects/{projectId}/system-prompts/{promptId}/priority` | 调整优先级 |
| `DELETE` | `/api/projects/{projectId}/system-prompts/{promptId}` | 删除 Prompt |

## 6.5 集成

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/ai/integrations` | 获取集成面板数据 |
| `GET` | `/api/integration-market` | 获取市场目录 |
| `GET` | `/api/integration-market/{itemId}` | 获取市场项详情 |
| `POST` | `/api/mcp-servers` | 注册自建 MCP 服务 |
| `PUT` | `/api/mcp-servers/{serverId}` | 编辑 MCP 服务 |
| `POST` | `/api/mcp-servers/{serverId}/test` | 测试连通性 |
| `POST` | `/api/mcp-servers/{serverId}/discover` | 发现能力 |
| `POST` | `/api/mcp-servers/{serverId}/authorize` | 保存授权态 |
| `POST` | `/api/projects/{projectId}/mcp-integrations` | 启用集成到项目 |
| `DELETE` | `/api/projects/{projectId}/mcp-integrations/{integrationId}` | 移除项目集成 |

## 7. 项目配额管理

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/quota/summary` | 获取项目池和成员视角配额总览 |
| `GET` | `/api/projects/{projectId}/quota/pools` | 获取项目共享池列表 |
| `PUT` | `/api/projects/{projectId}/quota/pools/{poolId}` | 编辑项目共享池 |
| `GET` | `/api/projects/{projectId}/quota/member-usage` | 获取成员消耗排行 |
| `GET` | `/api/projects/{projectId}/member-quotas` | 获取成员个人配额 |
| `PUT` | `/api/projects/{projectId}/members/{memberId}/quota` | 修改成员个人配额 |
| `PUT` | `/api/projects/{projectId}/quota/policies` | 保存项目配额策略 |

策略字段建议：

- `exhaustBehavior`
- `alertThresholdPercent`
- `allowManagerTemporaryBoost`
- `allowedModelIds`
- `rpmPerUser`
- `tpmPerUser`

## 8. 项目代码服务、事故、Agent

## 8.1 代码服务

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/services` | 获取服务列表 |
| `POST` | `/api/projects/{projectId}/services` | 创建服务 |
| `GET` | `/api/projects/{projectId}/services/{serviceId}` | 获取服务详情 |
| `PUT` | `/api/projects/{projectId}/services/{serviceId}` | 编辑服务 |
| `GET` | `/api/projects/{projectId}/services/{serviceId}/environments` | 获取环境列表 |
| `POST` | `/api/projects/{projectId}/services/{serviceId}/environments` | 新建环境 |

## 8.2 事故与告警

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/incidents` | 获取事故列表 |
| `GET` | `/api/projects/{projectId}/alerts` | 获取项目告警事件 |
| `POST` | `/api/projects/{projectId}/alerts/{alertId}/acknowledge` | 确认告警 |
| `POST` | `/api/projects/{projectId}/alerts/{alertId}/resolve` | 解决告警 |
| `POST` | `/api/projects/{projectId}/incidents/{incidentId}/ai-diagnose` | 触发 AI 诊断 |

## 8.3 乐知助手 / 项目 Agent

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/projects/{projectId}/agent` | 获取项目 Agent 配置 |
| `PUT` | `/api/projects/{projectId}/agent` | 编辑项目 Agent 配置 |
| `POST` | `/api/projects/{projectId}/agent/rebuild-prompt` | 重建项目 Agent Prompt |

## 9. 平台级 AI 治理

## 9.1 平台知识库、技能、工具、集成、原子能力、模板

平台级接口与项目级接口基本同形，但 `scope=GLOBAL` 且权限不同。

额外接口：

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/atomic-capabilities` | 获取原子能力列表 |
| `POST` | `/api/atomic-capabilities` | 创建原子能力 |
| `PUT` | `/api/atomic-capabilities/{capabilityId}` | 编辑原子能力 |
| `POST` | `/api/atomic-capabilities/{capabilityId}/publish` | 发布原子能力 |
| `POST` | `/api/projects/{projectId}/atomic-capabilities` | 项目订阅原子能力 |
| `GET` | `/api/templates` | 获取模板库列表 |
| `POST` | `/api/templates` | 创建模板 |
| `PUT` | `/api/templates/{templateId}` | 编辑模板 |
| `POST` | `/api/templates/{templateId}/archive` | 归档模板 |

## 9.2 上游资源治理

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/admin/providers` | 获取供应商列表 |
| `GET` | `/api/admin/models` | 获取模型列表 |
| `GET` | `/api/admin/provider-api-keys` | 获取上游 Key 列表 |
| `POST` | `/api/admin/provider-api-keys` | 创建上游 Key |
| `PUT` | `/api/admin/provider-api-keys/{keyId}` | 编辑上游 Key |
| `POST` | `/api/admin/provider-api-keys/{keyId}/rotate` | 轮换上游 Key |
| `GET` | `/api/admin/model-routing-policies` | 获取模型路由策略 |
| `POST` | `/api/admin/model-routing-policies` | 创建模型路由策略 |
| `PUT` | `/api/admin/model-routing-policies/{policyId}` | 编辑模型路由策略 |
| `GET` | `/api/admin/global-quota-policies` | 获取全局配额策略 |
| `PUT` | `/api/admin/global-quota-policies/{policyId}` | 编辑全局配额策略 |

## 10. 员工、平台角色、凭证、审计

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/api/admin/users` | 获取员工列表 |
| `POST` | `/api/admin/users` | 新增员工 |
| `PUT` | `/api/admin/users/{userId}` | 编辑员工 |
| `PATCH` | `/api/admin/users/{userId}/status` | 启停员工账号 |
| `GET` | `/api/admin/platform-roles` | 获取平台角色 |
| `PUT` | `/api/admin/platform-roles/{roleId}/permissions` | 保存平台角色权限 |
| `GET` | `/api/admin/credentials` | 获取全平台凭证列表 |
| `POST` | `/api/admin/credentials/{credentialId}/revoke` | 吊销凭证 |
| `GET` | `/api/admin/credential-rotation-logs` | 获取轮换日志 |
| `GET` | `/api/admin/activity-logs` | 获取活动日志 |
| `GET` | `/api/admin/audit-logs` | 获取审计日志 |
| `GET` | `/api/admin/security-events` | 获取安全事件 |
| `GET` | `/api/admin/alert-rules` | 获取告警规则 |
| `POST` | `/api/admin/alert-rules` | 创建告警规则 |

## 11. 运行面网关

## 11.1 AI 兼容网关

| 方法 | 路径 | 用途 |
|---|---|---|
| `POST` | `/proxy/openai/v1/chat/completions` | OpenAI 兼容网关 |
| `POST` | `/proxy/openai/v1/responses` | OpenAI Responses 兼容网关 |
| `POST` | `/proxy/anthropic/v1/messages` | Anthropic 兼容网关 |

公共 Header：

- `Authorization: Bearer {platform_credential}`
- `X-Project-ID: {projectCode}` 可选，未传时取 `default_project_id`

运行面副作用：

1. 校验平台凭证。
2. 加载当前项目的能力装配结果。
3. 执行双池配额检查。
4. 转发模型请求。
5. 落 `ai_usage_events`。

## 11.2 MCP 网关

| 方法 | 路径 | 用途 |
|---|---|---|
| `GET` | `/mcp/global` | 获取全局能力 |
| `GET` | `/mcp/project/{projectCode}` | 获取项目能力 |
| `GET` | `/mcp/third/{serverName}` | 获取第三方 MCP 能力代理 |
| `POST` | `/mcp/global/tools/call` | 调用全局工具 |
| `POST` | `/mcp/project/{projectCode}/tools/call` | 调用项目工具 |

## 11.3 内部装配接口

供运行面调用：

| 方法 | 路径 | 用途 |
|---|---|---|
| `POST` | `/internal/auth/resolve-credential` | 解析平台凭证 |
| `POST` | `/internal/project-context/resolve` | 解析当前项目上下文 |
| `GET` | `/internal/assembly/projects/{projectId}` | 获取项目能力装配结果 |
| `POST` | `/internal/quota/check` | 检查双池配额 |
| `POST` | `/internal/quota/commit` | 提交扣减结果 |

## 12. 实现要求

1. 所有写接口都必须返回变更后的最新对象，不要求前端额外再查一次。
2. 所有列表接口必须支持分页、关键字、状态、范围筛选。
3. 所有敏感写接口必须写 `audit_logs`。
4. 所有项目级接口都必须做项目成员权限校验，不能仅做登录校验。
5. 运行面不得直接信任前端透传的项目信息，必须再次校验成员对项目的访问权。
