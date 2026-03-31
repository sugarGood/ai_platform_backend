# 数据库结构优化设计

## 背景

当前仓库的数据库设计可以覆盖旧版平台能力，但新原型已经把以下关系明确下来：

- 平台 AI 资产被拆成全局知识库、能力与技能、工具注册中心、集成管理、原子能力中心、代码模板库。
- 项目空间需要继承全局知识库、平台能力和工具，并支持成员级单独调整。
- 能力和工具的配置不直接生效，而是经过提交审批、版本管理和统一发布。
- Knowledge Tool 和 Skill 均直接依赖已登记的知识库 collection。

现状存在三类问题：

1. 原型需要的核心对象尚未完整落库。
2. 部分关系被压缩在 JSON 字段中，难以做查询、审批和版本管理。
3. 部分表结构、Java 实体和服务逻辑已经出现不一致。

本设计目标是先把数据库骨架调整到可支撑新原型的状态，再按该骨架推进接口与服务层改造。

## 目标

- 保留当前主表和大部分现有 API 路径，避免一次性推翻现有代码。
- 补齐审批、版本、资源授权、集成授权、知识库 collection 等关键模型。
- 把核心关系从 JSON 拆到关系表，保留 JSON 只存可变配置。
- 优先解决影响后续模块开发的数据库缺口，而不是做全量重构。

## 非目标

- 本轮不直接重写全部 controller/service。
- 本轮不处理所有历史命名问题，只处理会阻塞原型落地的部分。
- 本轮不引入大规模数据清洗，只提供迁移路径。

## 方案对比

### 方案 A：保守补丁型

仅在现有表上补字段，例如：

- `knowledge_bases` 增加 `collection_id`
- `skills` 增加审批状态
- `tool_definitions` 增加版本号

优点：

- 改动最小
- 对现有代码冲击最小

缺点：

- 审批、版本、授权仍然会混在主表中
- 关系继续依赖 JSON
- 后续仍然需要二次拆表

### 方案 B：兼容演进型

保留当前主表和接口命名，在现有模型上补充关系表和版本表：

- 主表保存当前生效版本摘要
- 新增版本表保存待审批和历史版本
- 新增授权和资源范围表表达项目空间能力

优点：

- 能兼容当前代码结构
- 能对齐新原型的审批、继承、授权逻辑
- 后续 SQL 和服务改造都可渐进完成

缺点：

- 表数量会增加
- 需要设计迁移路径

### 方案 C：全量重构型

直接将现有结构切换到设计稿目标模型，例如：

- `tool_definitions` 直接替换为 `tools`
- 配额体系直接替换为 `project_token_pools`
- 角色权限全部按新表重建

优点：

- 模型最干净

缺点：

- 对现有代码冲击过大
- 本轮成本和风险都偏高

## 推荐方案

采用方案 B：兼容演进型。

原因：

1. 新原型已经明确要求审批发布、知识库 collection 绑定、项目继承和成员级授权。
2. 现有仓库已经有不少主表和基础接口，保留这些主干可以减少改造面。
3. 现有 SQL 与实体之间已经出现不一致，先做兼容演进比直接全量切换更稳妥。

## 关键问题与设计决策

### 1. 知识库需要引入 collection 主键语义

现有 `knowledge_bases` 只有业务名称和 `scope/project_id`，但原型已经要求 Skill 和 Knowledge Tool 直接绑定 collection。

决策：

- 在 `knowledge_bases` 中新增 `collection_id`
- 将 `collection_id` 作为知识检索边界的稳定标识
- 保持 `id` 作为业务主键，`collection_id` 作为向量检索主键

### 2. Skill 和 Tool 需要从“单表当前态”升级为“主表 + 版本表”

现有 `skills` 只有一行数据表示当前状态，`tool_definitions` 甚至没有审批语义，无法支撑“提交审批后上线”。

决策：

- `skills`、`tool_definitions` 保留为主表
- 新增 `skill_versions`、`tool_versions`
- 主表仅保存当前生效版本引用和摘要信息
- 待审批、驳回、历史版本全部进入版本表

### 3. 项目空间能力控制不能只靠启用表

现有 `project_skills`、`project_tools`、`project_knowledge_configs` 只能表示项目是否启用，无法表达：

- 项目角色默认能力
- 成员个体覆盖
- 知识库/技能/工具/集成的资源级授权

决策：

- 新增 `project_role_templates`
- 新增 `project_role_template_permissions`
- 新增 `project_member_permission_overrides`
- 新增 `project_member_resource_grants`

### 4. 集成管理需要拆分市场项、连接实例、授权和项目启用

现有 `mcp_servers` 与 `project_mcp_integrations` 还不足以表达：

- 集成市场目录
- 不同认证方式的连接授权
- 一个连接停用后下游工具回收

决策：

- 保留 `mcp_servers`
- 新增 `integration_market_items`
- 新增 `mcp_server_authorizations`
- `project_mcp_integrations` 只保留项目启用和项目级覆盖配置

### 5. 核心关系不能继续只依赖 JSON

现有以下字段不适合继续承载核心关系：

- `skills.knowledge_refs`
- `skills.bound_tools`
- `tool_definitions.impl_config` 中的来源关系

决策：

- 新增 `skill_version_tools`
- 新增 `skill_version_knowledge_refs`
- JSON 仅保留执行配置、动态参数和非结构化扩展信息

## 表级改造设计

### A. 修改 `knowledge_bases`

新增字段：

- `collection_id varchar(128) not null`
- `vector_store varchar(32) null`
- `embedding_dimension int unsigned null`
- `inheritance_mode varchar(32) null`
- `visibility_scope varchar(32) null`
- `published_flag tinyint(1) not null default 1`

约束：

- `collection_id` 唯一
- `scope='GLOBAL'` 时 `project_id is null`
- `scope='PROJECT'` 时 `project_id is not null`

索引：

- `uk_collection_id(collection_id)`
- `idx_scope_status_project(scope, status, project_id)`

### B. 修改 `kb_documents`

新增字段：

- `source_type varchar(32)`
- `storage_provider varchar(32)`
- `content_hash varchar(128)`
- `parser_version varchar(32)`
- `vectorized_at datetime`
- `acl_scope varchar(32)`
- `approval_status varchar(32)`

索引：

- `idx_kb_status_updated(kb_id, status, updated_at)`
- `idx_content_hash(content_hash)`

### C. 修改 `skills`

新增字段：

- `current_version_id bigint unsigned null`
- `approval_status varchar(32) not null default 'DRAFT'`
- `source_type varchar(32) null`
- `owner_type varchar(32) null`
- `owner_id bigint unsigned null`

保留字段：

- `skill_key`
- `name`
- `description`
- `scope`
- `project_id`
- `category`
- `status`

迁移策略：

- 现有 `system_prompt`、`knowledge_refs`、`bound_tools`、`parameters` 先保留
- 迁移完成后逐步改为只读镜像或废弃

### D. 新增 `skill_versions`

核心字段：

- `id`
- `skill_id`
- `version_no`
- `system_prompt`
- `execution_type`
- `main_tool_id`
- `config_json`
- `status`
- `submitted_by`
- `submitted_at`
- `approved_by`
- `approved_at`
- `created_at`
- `updated_at`

状态建议：

- `DRAFT`
- `PENDING_APPROVAL`
- `APPROVED`
- `REJECTED`
- `PUBLISHED`
- `DEPRECATED`

约束：

- `uk_skill_version(skill_id, version_no)`

### E. 新增 `skill_version_tools`

核心字段：

- `id`
- `skill_version_id`
- `tool_id`
- `relation_type`
- `sort_order`

约束：

- `uk_skill_version_tool(skill_version_id, tool_id, relation_type)`

说明：

- `relation_type` 取值 `PRIMARY` / `AUXILIARY`
- 用于替代当前 `bound_tools` 的结构性部分

### F. 新增 `skill_version_knowledge_refs`

核心字段：

- `id`
- `skill_version_id`
- `knowledge_base_id`
- `collection_id`
- `top_k`
- `prompt_template`
- `sort_order`

说明：

- 允许同时保留 `knowledge_base_id` 和 `collection_id`
- `collection_id` 用于向量检索
- `knowledge_base_id` 用于业务管理和权限关联

### G. 修改 `tool_definitions`

新增字段：

- `tool_type varchar(32) not null default 'API'`
- `provider_type varchar(32) null`
- `integration_id bigint unsigned null`
- `current_version_id bigint unsigned null`
- `approval_status varchar(32) not null default 'APPROVED'`
- `visibility_scope varchar(32) null`

说明：

- 保留表名 `tool_definitions`，降低现有代码改造成本
- 后续如需对齐命名，可通过视图或二次迁移切换到 `tools`

### H. 新增 `tool_versions`

核心字段：

- `id`
- `tool_id`
- `version_no`
- `input_schema`
- `output_schema`
- `impl_type`
- `impl_config`
- `status`
- `submitted_by`
- `submitted_at`
- `approved_by`
- `approved_at`
- `created_at`
- `updated_at`

### I. 新增 `integration_market_items`

核心字段：

- `id`
- `item_code`
- `item_name`
- `item_type`
- `source_server_id`
- `category`
- `icon_url`
- `description`
- `auth_type`
- `status`
- `created_at`
- `updated_at`

说明：

- 一个市场项对应一个可上架的集成条目
- `mcp_servers` 仍然表示可连接的实际服务定义

### J. 新增 `mcp_server_authorizations`

核心字段：

- `id`
- `mcp_server_id`
- `subject_type`
- `subject_id`
- `auth_type`
- `auth_payload`
- `status`
- `expires_at`
- `last_verified_at`
- `created_by`
- `created_at`
- `updated_at`

说明：

- 用于表达 OAuth2 / Bearer / API Key 等授权状态
- 不直接决定项目是否启用

### K. 修改 `project_mcp_integrations`

保留并使用已有字段：

- `custom_config`
- `permission_scope`
- `connected_at`

说明：

- 数据库已有这些字段
- Java 实体需要补齐映射

### L. 新增项目空间权限表

新增表：

- `project_role_templates`
- `project_role_template_permissions`
- `project_member_permission_overrides`
- `project_member_resource_grants`

资源范围建议：

- `KNOWLEDGE_BASE`
- `SKILL`
- `TOOL`
- `INTEGRATION`
- `ATOMIC_CAPABILITY`

## 一致性修复

在表结构演进前，先修两处现有不一致：

1. `project_mcp_integrations` 数据库字段与 `ProjectMcpIntegration` 实体不一致。
2. `tool_definitions.status` 数据库枚举为 `ACTIVE/DISABLED`，但服务层仍写入 `INACTIVE`。

这两处不先修，后续数据库优化会被现有代码继续污染。

## 索引策略

本轮索引只围绕原型明确的查询路径加，不做过度优化。

建议新增或确认以下索引：

- `knowledge_bases(scope, status, project_id)`
- `knowledge_bases(collection_id)`
- `kb_documents(kb_id, status, updated_at)`
- `skills(scope, project_id, status)`
- `skill_versions(skill_id, status, approved_at)`
- `tool_definitions(scope, project_id, status)`
- `tool_versions(tool_id, status, approved_at)`
- `project_role_templates(project_id, status)`
- `project_member_resource_grants(project_member_id, resource_type, resource_id)`
- `mcp_server_authorizations(mcp_server_id, subject_type, subject_id, status)`

## 数据迁移顺序

### 第一阶段：修正现有不一致

- 修复实体和枚举值
- 不改数据结构语义

### 第二阶段：补主干字段

- 修改 `knowledge_bases`
- 修改 `kb_documents`
- 修改 `skills`
- 修改 `tool_definitions`

### 第三阶段：新增版本与关系表

- 新增 `skill_versions`
- 新增 `skill_version_tools`
- 新增 `skill_version_knowledge_refs`
- 新增 `tool_versions`

### 第四阶段：补集成与权限模型

- 新增 `integration_market_items`
- 新增 `mcp_server_authorizations`
- 新增项目空间权限表

### 第五阶段：迁移历史数据

- 从 `skills` 生成首版 `skill_versions`
- 从 `tool_definitions` 生成首版 `tool_versions`
- 从 `knowledge_refs` 和 `bound_tools` 迁移关系数据

## 风险与控制

### 风险 1：现有接口仍依赖旧字段

控制：

- 主表保留旧字段，先做双写或镜像迁移
- 等服务层完成后再清理冗余字段

### 风险 2：历史 JSON 数据结构不统一

控制：

- 迁移脚本按容错方式处理
- 无法解析的数据进入人工校验清单

### 风险 3：权限模型一次上得太重

控制：

- 先落表和默认模板
- 成员个体覆盖可以在第二批接口中启用

## 后续执行建议

后续按以下顺序落地：

1. 先出数据库迁移 SQL 草案。
2. 再修实体与 mapper。
3. 再修改服务层读写逻辑。
4. 最后补接口和测试。

这能保证数据库先成为稳定基线，避免边开发边返工表结构。
