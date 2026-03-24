# 原型对照开发清单

## 1. 使用方式

本文件给研发直接做对照开发。

使用顺序：

1. 先找到原型页面。
2. 看本页对应的读接口、写接口和数据表。
3. 实现页面主动作。
4. 补日志、审计、异步任务和配额扣减。

## 2. 个人工作台

## 2.1 我的凭证

### 页面关注点

- 一人一证
- 多项目共用
- 当前工作项目切换
- Claude Code / Cursor / Codex 接入材料
- 一键安装
- 连通性测试

### 读接口

- `GET /api/me/credential`
- `GET /api/me/setup-materials`

### 写接口

- `POST /api/me/credential/rotate`
- `POST /api/me/credential/renew`
- `POST /api/me/credential/test`
- `PUT /api/me/current-project`
- `POST /api/me/setup-script`

### 涉及表

- `platform_credentials`
- `credential_project_scopes`
- `client_apps`
- `user_client_bindings`
- `projects`

### 必做副作用

- 轮换写 `credential_rotation_logs`
- 切换项目更新 `default_project_id`
- 测试连通性更新 `last_active_at`
- 写 `activity_logs`

## 2.2 我的能力

### 页面关注点

- 可用知识库
- 可用技能
- 可用工具
- 可用集成
- 当前项目下自动注入范围

### 读接口

- `GET /api/me/abilities`

### 涉及表

- `project_members`
- `project_role_templates`
- `project_role_template_permissions`
- `project_member_permission_overrides`
- `project_member_resource_grants`
- `project_knowledge_configs`
- `project_skills`
- `project_tools`
- `project_mcp_integrations`

### 开发要点

1. 先算角色模板默认能力。
2. 再叠加成员覆写。
3. 最后按资源授权范围收敛。

## 2.3 我的用量

### 页面关注点

- Token 总量
- API 请求次数
- 技能调用
- 按项目分布

### 读接口

- `GET /api/me/usage/summary`
- `GET /api/me/usage/trend`
- `GET /api/me/usage/events`
- `GET /api/me/usage/projects`

### 涉及表

- `ai_usage_events`
- `ai_usage_daily`

## 3. 平台页面

## 3.1 项目空间

### 页面关注点

- 项目列表
- 模板建项
- 初始化知识库和技能

### 读接口

- `GET /api/projects`

### 写接口

- `POST /api/projects`
- `PUT /api/projects/{projectId}`
- `POST /api/projects/{projectId}/archive`

### 涉及表

- `projects`
- `project_role_templates`
- `project_token_pools`
- `project_templates`
- `activity_logs`

## 3.2 全局知识库

### 页面关注点

- 知识库列表
- 分类和文档管理
- 检索测试
- 命中率统计

### 读接口

- `GET /api/knowledge-bases?scope=GLOBAL`
- `GET /api/knowledge-bases/{knowledgeBaseId}`
- `GET /api/knowledge-bases/{knowledgeBaseId}/documents`

### 写接口

- `POST /api/knowledge-bases`
- `PUT /api/knowledge-bases/{knowledgeBaseId}`
- `POST /api/knowledge-bases/{knowledgeBaseId}/documents`
- `DELETE /api/knowledge-bases/{knowledgeBaseId}/documents/{documentId}`
- `POST /api/knowledge-bases/{knowledgeBaseId}/search-test`

### 涉及表

- `knowledge_bases`
- `kb_documents`
- `knowledge_search_logs`

### 异步任务

- 文档解析
- 分块
- 向量化
- 重建索引

## 3.3 全局技能库

### 页面关注点

- 草稿
- 测试
- 审核
- 发布
- 克隆到项目
- 满意度反馈

### 涉及表

- `skills`
- `skill_feedback`
- `activity_logs`
- `audit_logs`

## 3.4 全局工具集

### 页面关注点

- 工具注册
- 启停
- 输入 Schema
- 实现方式
- 调用日志

### 涉及表

- `tools`
- `tool_invocation_logs`

## 3.5 集成市场

### 页面关注点

- 市场目录
- 认证方式
- 一键接入
- 项目启用

### 涉及表

- `integration_market_items`
- `mcp_servers`
- `mcp_server_authorizations`
- `project_mcp_integrations`

## 3.6 原子能力中心

### 页面关注点

- 能力目录
- 接入文档
- 项目订阅

### 涉及表

- `atomic_capabilities`
- `project_atomic_capabilities`

## 3.7 代码模板库

### 页面关注点

- 模板列表
- 使用模板建项目
- 使用模板建服务

### 涉及表

- `project_templates`
- `projects`
- `services`

## 3.8 员工管理

### 页面关注点

- 平台总人数
- 平台角色
- 凭证状态
- 最后活跃
- 批量操作

### 涉及表

- `users`
- `departments`
- `platform_roles`
- `platform_credentials`
- `project_members`

## 3.9 凭证管理

### 页面关注点

- 个人凭证
- 服务账号
- 临时凭证
- 过期提醒
- 轮换和吊销

### 涉及表

- `platform_credentials`
- `credential_project_scopes`
- `credential_rotation_logs`

## 3.10 权限管理

### 页面关注点

- 平台角色权限
- 项目默认角色模板
- 权限矩阵
- 权限变更日志

### 涉及表

- `permission_definitions`
- `platform_roles`
- `platform_role_permissions`
- `project_role_templates`
- `project_role_template_permissions`
- `activity_logs`
- `audit_logs`

## 3.11 审计安全

### 页面关注点

- 审计日志
- 安全规则
- 安全事件

### 涉及表

- `audit_logs`
- `security_rules`
- `security_events`

## 3.12 设置

### 页面关注点

- 模型路由配置
- 上游 Key 管理
- 全局配额策略

### 涉及表

- `ai_providers`
- `ai_models`
- `provider_api_keys`
- `model_routing_policies`
- `global_quota_policies`

## 3.13 Agent 工作流

### 页面关注点

- 工作流定义
- 节点设计
- 执行记录

### 涉及表

- `workflow_definitions`
- `workflow_nodes`
- `workflow_executions`
- `workflow_execution_steps`

## 4. 项目页面

## 4.1 概览

### 页面关注点

- 项目状态
- 风险概览
- 成员数
- AI 能力启用概览

### 读接口

- `GET /api/projects/{projectId}/overview`

### 聚合来源

- `projects`
- `project_members`
- `knowledge_bases`
- `project_skills`
- `project_tools`
- `project_mcp_integrations`
- `project_token_pools`
- `incidents`
- `alert_events`

## 4.2 接入与凭证

### 页面关注点

- 本项目 MCP 端点
- 项目接入说明
- 批量发送接入指南
- 成员接入状态
- 服务账号

### 涉及表

- `platform_credentials`
- `credential_project_scopes`
- `user_client_bindings`
- `project_members`
- `client_apps`

## 4.3 AI 能力

### 页面关注点

- 知识库
- 技能库
- 工具集
- System Prompt
- 集成

### 涉及表

- `knowledge_bases`
- `kb_documents`
- `project_knowledge_configs`
- `skills`
- `project_skills`
- `tools`
- `project_tools`
- `project_system_prompts`
- `mcp_servers`
- `mcp_server_authorizations`
- `project_mcp_integrations`

### 开发要点

1. 五个面板必须分别可独立加载和保存。
2. 全局继承和项目自有必须在接口里显式区分。
3. 继承项和项目自有项在前端是同屏展示，接口要给清楚 `source`。

## 4.4 配额管理

### 页面关注点

- 项目共享池
- 成员消耗排行
- 个人额度
- 超限策略

### 涉及表

- `project_token_pools`
- `member_ai_quotas`
- `ai_usage_events`
- `ai_usage_daily`

### 开发要点

1. 页面里的“项目池”和“个人池”不能混成一个字段。
2. 请求执行时必须同时校验两者。

## 4.5 代码服务

### 页面关注点

- 服务列表
- 环境
- 仓库
- 技术栈

### 涉及表

- `services`
- `service_environments`

## 4.6 事故与告警

### 页面关注点

- 事故列表
- 告警事件
- AI 诊断

### 涉及表

- `incidents`
- `alert_rules`
- `alert_events`
- `notification_channels`

## 4.7 成员权限

### 页面关注点

- 成员列表
- 角色权限
- 权限矩阵
- 个体能力范围
- 成员变更日志

### 涉及表

- `project_members`
- `project_role_templates`
- `project_role_template_permissions`
- `project_member_permission_overrides`
- `project_member_resource_grants`
- `member_ai_quotas`
- `activity_logs`

### 开发要点

1. 角色模板页和成员个体页不能共用一套保存逻辑。
2. 成员改角色时，支持选择是否同步恢复为角色默认能力。
3. 个体资源授权必须支持知识库、技能、工具三个清单。

## 4.8 项目设置

### 页面关注点

- 项目基础信息
- 默认 Prompt 和策略
- 默认模型

### 涉及表

- `projects`
- `project_system_prompts`
- `model_routing_policies`

## 4.9 乐知助手

### 页面关注点

- 项目 Agent 设定
- 对话入口
- 调用知识库和监控能力

### 涉及表

- `project_agents`
- `project_system_prompts`
- `knowledge_search_logs`
- `ai_usage_events`

## 5. 运行链路对照

## 5.1 一次 AI 请求

### 必经步骤

1. 解析凭证。
2. 解析项目上下文。
3. 拉取项目能力装配结果。
4. 校验项目池和个人池。
5. 注入 Prompt、知识、工具、集成。
6. 转发模型请求。
7. 写用量、检索、工具调用日志。

### 最少写表

- `platform_credentials`
- `project_members`
- `project_token_pools`
- `member_ai_quotas`
- `ai_usage_events`

### 可能写表

- `knowledge_search_logs`
- `tool_invocation_logs`

## 6. 本轮不作为核心主链路的扩展项

历史 SQL 中的以下方向可后续独立扩展，不作为当前主链路主设计：

- 研发效能看板
- CI/CD 流水线明细
- Sprint / Epic / Task 管理
- DORA 指标
- AI 评测分数

这些能力可以作为项目服务或运营看板的扩展域，不应反向主导 AI 能力平台的核心表设计。
