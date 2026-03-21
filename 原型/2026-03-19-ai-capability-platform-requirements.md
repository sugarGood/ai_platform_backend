# AI 能力平台需求文档
# AI Capability Platform — Product Requirements Document

- 文档版本：v1.0
- 文档日期：2026-03-19
- 文档类型：产品需求文档（PRD）
- 关联文档：`2026-03-18-enterprise-ai-workspace-platform-design.md`
- 适用范围：AI 能力体系（知识库 / 技能库 / 工具集 / 原子能力 / MCP）及凭证接入全链路

---

## 目录

1. 背景与目标
2. 核心概念与术语
3. 整体架构
4. 凭证分发与接入体系
5. 知识库模块
6. 技能库模块
7. 工具集模块
8. 企业级原子能力模块
9. MCP 集成模块
10. 代理微服务设计
11. 平台 MCP 统一网关设计
12. 产品信息架构
13. 核心用户流程
14. 数据模型
15. 接口规范
16. 非功能性需求
17. 开发优先级与里程碑

---

## 1. 背景与目标

### 1.1 背景

企业成员日常使用 Claude Code、Cursor、Codex 等 AI 工具进行研发工作，当前存在以下问题：

- 成员各自使用个人 API Key，企业无法统一管控与追踪
- AI 工具无法感知项目上下文，知识割裂、重复建设
- 技能（Prompt 模板）、工具集（Function Tools）、外部集成（MCP）各自分散，无法复用
- 企业积累的知识资产、规范文档无法有效注入 AI 工作流
- 成员接入配置复杂，体验差，门槛高

### 1.2 目标

建设企业 AI 能力平台，实现：

1. **统一凭证分发**：成员通过平台分发的凭证访问 AI 能力，不直接持有上游真实 Key
2. **知识库体系**：企业全局知识库 + 项目级知识库，自动注入 AI 上下文
3. **技能库体系**：企业全局技能 + 项目技能，标准化 AI 工作流
4. **工具集体系**：统一管理 Function Tools，按权限按需注入
5. **原子能力标准**：企业级基础 AI 能力沉淀，供项目和 Agent 快速复用
6. **MCP 统一接入**：自建 MCP + 第三方 MCP 统一纳管，通过标准协议暴露所有能力
7. **一键接入体验**：成员 3 分钟内完成工具配置，开箱即用

### 1.3 成功指标

| 指标 | 目标值 |
|------|--------|
| 成员接入耗时 | ≤ 3 分钟 |
| 知识库命中率 | ≥ 70% |
| 技能复用率 | 企业技能月均使用次数 ≥ 50 次/技能 |
| 凭证安全事件 | 0 起真实 Key 泄露 |
| 平台代理延迟增量 | ≤ 200ms P99 |

---

## 2. 核心概念与术语

| 术语 | 定义 | 对用户的呈现 |
|------|------|-------------|
| **平台凭证（Credential）** | 平台为成员生成的接入凭证，映射至内部权限体系 | 我的凭证 / API Key |
| **知识库（Knowledge Base）** | 企业/项目文档的向量化存储，支持语义检索 | 知识库 |
| **技能（Skill）** | 面向业务目标的 Prompt 模板 + 上下文 + 可选工具的组合 | 技能 |
| **工具（Tool）** | AI 可调用的原子动作，基于 Function Calling 协议 | 工具 |
| **原子能力（Atomic Capability）** | 企业级标准化底层 AI 能力，可被项目和 Agent 订阅复用 | 平台基础能力 |
| **MCP 服务器** | 遵循 Model Context Protocol 的工具服务，统一暴露 Tools/Resources/Prompts | 集成 |
| **代理微服务（Proxy Service）** | 独立微服务，拦截 AI API 请求，注入能力、追踪用量、验证权限 | 不对用户暴露 |
| **平台 MCP 网关** | 统一暴露平台所有能力（知识库/技能/工具）的 MCP 服务器入口 | 集成地址 |

### 2.1 技术概念澄清

```
Function Tools（功能工具）
  └─ 定义：在 AI API 请求体中声明的可调用函数（JSON Schema 格式）
  └─ 作用：AI 决策后调用，由平台执行并将结果回传 AI
  └─ 对用户：归入"工具"模块，不暴露技术细节

MCP（Model Context Protocol）
  └─ 定义：暴露 Tools/Resources/Prompts 的传输协议（JSON-RPC 2.0）
  └─ 作用：客户端（Claude Code/Cursor）自动发现并调用平台能力
  └─ 对用户：归入"集成"模块，叫"集成"不叫"MCP"

Skills（技能）
  └─ 定义：平台业务层概念，通过 MCP Prompts 类型暴露
  └─ 作用：激活预设的工作流模板，注入上下文和工具
  └─ 对用户：技能库，可通过 /skill 命令触发

三者关系：
  Skill（业务目标层）
    └─ 通过 MCP Prompts 暴露
         └─ 触发后调用 Function Tools
              └─ 执行实际业务逻辑（查知识库、调 API 等）
```

---

## 3. 整体架构

```
┌──────────────────────────────────────────────────────────────────┐
│                         成员侧工具                               │
│        Claude Code          Cursor          Codex / Agent        │
│    ANTHROPIC_BASE_URL    Custom API URL    OPENAI_BASE_URL        │
│    平台凭证 Key           平台凭证 Key      平台凭证 Key           │
└───────────────────────────────┬──────────────────────────────────┘
                                │ HTTPS
┌───────────────────────────────▼──────────────────────────────────┐
│                      代理微服务（独立部署）                        │
│                                                                  │
│  /proxy/anthropic/*   ─────────────────────→  Anthropic API     │
│  /proxy/openai/*      ─────────────────────→  OpenAI API        │
│                                                                  │
│  职责：                                                           │
│  ① 验证平台凭证，解析成员身份与项目权限                            │
│  ② 注入 System Prompt（全局规范 + 项目上下文摘要）                 │
│  ③ 注入 Function Tools（按权限过滤后的工具列表）                   │
│  ④ 拦截 tool_call，路由至平台 MCP 网关执行                         │
│  ⑤ 追踪 Token 用量，写入计费与审计                                 │
│  ⑥ 执行配额限制                                                   │
└───────────────────────────────┬──────────────────────────────────┘
                                │
┌───────────────────────────────▼──────────────────────────────────┐
│                    平台 MCP 统一网关（独立部署）                    │
│                                                                  │
│  MCP Prompts  ──→  技能库（Skill 模板）                            │
│  MCP Tools    ──→  工具集（Function Tools 实现）                   │
│  MCP Resources──→  知识库（文档/代码索引）                         │
│                                                                  │
│  路由规则：                                                        │
│  /mcp/global            企业全局能力                              │
│  /mcp/project/{id}      项目级能力（含继承全局）                    │
│  /mcp/third/{name}      第三方 MCP 服务器代理                      │
└───────────┬──────────────────────┬───────────────────────────────┘
            │                      │
┌───────────▼──────┐  ┌────────────▼────────────────────────────┐
│   知识库服务      │  │         现有后端（Spring Boot）            │
│  （独立部署）     │  │                                          │
│                  │  │  Skills CRUD    Tools 配置注册            │
│  Vector DB       │  │  原子能力管理   凭证管理                   │
│  Embedding 服务  │  │  成员权限       用量统计                   │
│  文档解析 Pipeline│  │  审计日志       项目管理                   │
│  检索 API        │  └──────────────────────────────────────────┘
└──────────────────┘
```

---

## 4. 凭证分发与接入体系

### 4.1 凭证设计

#### 凭证结构

```
平台凭证（Platform Credential）
├── Key 格式：plt_{uid}_{random}_{checksum}
├── 携带信息：
│   ├── 成员 ID
│   ├── 所属项目列表（权限由后端动态判断）
│   └── 凭证类型（personal / service_account）
└── 不携带：真实上游 API Key（后端持有，不下发）
```

#### 凭证类型

| 类型 | 使用者 | 有效期 | 说明 |
|------|--------|--------|------|
| 个人凭证 | 员工个人 | 30/90 天（可配置） | 绑定个人身份，支持续签 |
| 服务账号凭证 | CI/CD、自动化脚本、Agent | 自定义（需审批） | 不绑定个人，绑定项目角色 |
| 临时凭证 | 体验 / 临时协作 | 24 小时 | 权限受限，无需审批 |

#### 凭证生命周期

```
申请 → 生成 → 激活 → 正常使用 → [续签 | 吊销 | 过期]
                                        ↓
                               凭证历史归档
```

### 4.2 接入材料

成员在"接入指南"页面选择工具后，平台自动生成对应接入材料：

#### Claude Code 接入材料

```bash
# 环境变量配置（写入 ~/.bashrc 或 ~/.zshrc）
export ANTHROPIC_BASE_URL="https://ai-platform.com/proxy/anthropic"
export ANTHROPIC_API_KEY="plt_uid_xxxxx"
```

```jsonc
// ~/.claude/settings.json（自动生成）
{
  "mcpServers": {
    "platform-global": {
      "url": "https://ai-platform.com/mcp/global",
      "headers": { "Authorization": "Bearer plt_uid_xxxxx" }
    },
    "platform-project": {
      "url": "https://ai-platform.com/mcp/project/proj_123",
      "headers": { "Authorization": "Bearer plt_uid_xxxxx" }
    }
  }
}
```

#### Cursor 接入材料

```
Settings → Models → OpenAI API Key: plt_uid_xxxxx
Settings → Models → Override OpenAI Base URL: https://ai-platform.com/proxy/openai
Settings → Features → MCP Servers → Add: https://ai-platform.com/mcp/project/proj_123
```

#### Codex / 自建 Agent 接入材料

```bash
export OPENAI_BASE_URL="https://ai-platform.com/proxy/openai"
export OPENAI_API_KEY="plt_uid_xxxxx"
```

#### 一键安装脚本

```bash
# 平台生成，成员复制执行（包含凭证，单次有效）
curl -fsSL "https://ai-platform.com/setup?token=SETUP_TOKEN" | bash
```

脚本自动完成：
1. 写入环境变量到 shell 配置文件
2. 生成 ~/.claude/settings.json（MCP 配置）
3. 验证连通性
4. 输出接入成功确认

### 4.3 接入状态

平台实时展示每个成员的接入状态：

| 状态 | 含义 |
|------|------|
| 未配置 | 成员尚未下载接入材料 |
| 已配置 | 已下载材料，未验证连通 |
| 正常 | 近期有正常调用记录 |
| 凭证即将过期 | 距过期 ≤ 7 天 |
| 凭证已过期 | 凭证失效，需续签 |
| 已吊销 | 管理员手动吊销 |

---

## 5. 知识库模块

### 5.1 模块定位

知识库是平台的信息资产层，支撑 AI 工具在回答问题时调取企业上下文，提升准确性和一致性。

### 5.2 分层架构

```
知识库层级
├── 企业全局知识库（Enterprise Global KB）
│   ├── 管理者：平台管理员
│   ├── 可见范围：全企业所有项目
│   └── 典型内容：
│       ├── 公司制度与行为规范
│       ├── 技术规范与编码标准
│       ├── 产品线文档
│       └── 安全合规要求
│
└── 项目级知识库（Project KB）
    ├── 管理者：项目管理员
    ├── 可见范围：当前项目成员
    └── 典型内容：
        ├── 项目需求文档
        ├── 系统架构设计
        ├── 代码库索引
        ├── 接口文档
        └── 业务规则说明
```

### 5.3 技术方案

#### RAG Pipeline

```
文档输入
  └─→ 文档解析（PDF/Word/Markdown/代码）
        └─→ 智能分块（Chunking，保留语义完整性）
              └─→ Embedding 向量化（BGE-M3，支持中英文）
                    └─→ 存入 Vector DB（pgvector）
                          ↕
                    语义检索 + 关键词检索混合召回
                          └─→ Reranking 重排序
                                └─→ 注入 AI 上下文
```

#### 技术选型

| 组件 | 选型 | 理由 |
|------|------|------|
| Vector DB | pgvector（PostgreSQL 扩展） | 复用现有数据库基础设施，降低运维复杂度 |
| Embedding | BGE-M3 | 中英文双语效果好，可本地部署，降低成本 |
| 文档解析 | Apache Tika + LlamaIndex | 支持多格式，开源成熟 |
| 检索策略 | 向量检索 + BM25 混合 | 提升召回率 |
| Reranking | BGE-Reranker | 精排提升相关性 |

### 5.4 知识库管理功能需求

#### 企业全局知识库管理（平台管理员）

| 功能 | 描述 |
|------|------|
| 文档上传 | 支持 PDF/Word/Markdown/TXT/代码文件，批量上传 |
| 分类管理 | 支持目录树结构组织文档 |
| 版本管理 | 文档更新后自动重新向量化，保留版本历史 |
| 检索测试 | 在线输入 Query 测试检索效果，可调整参数 |
| 访问统计 | 哪些文档被命中最多，命中后 AI 回答质量反馈 |

#### 项目级知识库管理（项目管理员）

| 功能 | 描述 |
|------|------|
| 文档上传 | 同上，仅对本项目成员可见 |
| 代码库索引 | 接入代码仓库（Git），自动索引代码结构和注释 |
| 继承全局 | 项目知识库默认继承全局知识库，可按类别关闭 |
| 检索权重 | 项目文档 vs 全局文档的检索权重可配置 |

### 5.5 知识库接入方式

知识库通过三种方式注入 AI：

```
方式 1：System Prompt 注入（代理层自动）
  ─ 适用：高频基础知识（如编码规范、项目简介）
  ─ 策略：每次请求自动附带精简摘要（≤ 500 tokens）

方式 2：MCP Resource 按需拉取（成员主动）
  ─ 适用：大型文档、按需查询
  ─ 触发：AI 判断需要时调用 search_knowledge Tool

方式 3：本地文件（项目仓库内置）
  ─ 适用：项目特定上下文
  ─ 形式：.claude/CLAUDE.md 或 .knowledge/ 目录
```

---

## 6. 技能库模块

### 6.1 模块定位

技能库是平台的工作流模板层，将企业沉淀的 AI 使用经验标准化，让成员开箱即用，无需重复编写 Prompt。

### 6.2 分层架构

```
技能层级
├── 企业全局技能（Enterprise Global Skills）
│   ├── 管理者：平台管理员
│   ├── 可见范围：全企业
│   └── 典型技能：
│       ├── 代码审查（code-review）
│       ├── 需求分析（requirement-analysis）
│       ├── 技术方案评审（tech-review）
│       ├── 文档生成（doc-generation）
│       ├── Bug 复现分析（bug-analysis）
│       └── 安全扫描（security-scan）
│
└── 项目级技能（Project Skills）
    ├── 管理者：项目管理员
    ├── 可见范围：当前项目
    └── 典型技能：
        ├── 项目专属代码审查（带项目规范）
        ├── 业务逻辑分析（带领域知识）
        └── 自定义工作流
```

### 6.3 技能定义规范

```yaml
# 技能定义格式（YAML）
skill:
  id: "code-review"
  name: "代码审查"
  description: "对提交的代码进行全面审查，包含规范、安全、性能三个维度"
  scope: "global"               # global | project:{projectId}
  category: "engineering"       # 技能分类
  version: "1.0.0"

  # 角色定义
  system_prompt: |
    你是一位资深工程师，负责对代码进行专业审查。
    请从以下三个维度给出具体意见：
    1. 编码规范：是否符合{{company_coding_standard}}
    2. 安全风险：是否存在 OWASP Top 10 相关漏洞
    3. 性能问题：是否有明显的性能瓶颈
    {{#if project_context}}项目背景：{{project_context}}{{/if}}

  # 自动注入的知识库
  knowledge_refs:
    - "global://tech-standards/coding-standard"
    - "project://architecture"            # 可选，仅项目技能有效

  # 绑定的可用工具
  tools:
    - "search_knowledge"
    - "post_code_comment"                 # 可选

  # 用户可配置参数
  parameters:
    - name: "language"
      type: "string"
      label: "编程语言"
      default: "auto"
    - name: "severity"
      type: "enum"
      options: ["high-only", "all"]
      default: "all"

  # 触发方式（MCP Prompt 类型）
  trigger:
    slash_command: "/code-review"
    description: "对当前代码进行审查"
```

### 6.4 技能管理功能需求

| 功能 | 描述 |
|------|------|
| 技能创建 | 可视化编辑器编写 Prompt 模板，支持变量语法 |
| 技能测试 | 在线试运行，实时预览 AI 输出效果 |
| 技能发布 | 草稿 → 审核 → 发布流程（全局技能需审核） |
| 技能克隆 | 基于全局技能创建项目定制版本 |
| 技能订阅 | 项目可订阅全局技能，自动获取更新 |
| 版本管理 | 技能变更有版本历史，支持回滚 |
| 使用统计 | 调用次数、使用用户、用户反馈（👍/👎） |

### 6.5 技能在客户端的呈现

在 Claude Code 中，技能通过 MCP Prompts 自动出现在 `/` 命令列表：

```
成员输入 / 后看到：
├── /code-review      代码审查（企业通用）
├── /doc-gen          文档生成（企业通用）
├── /bug-analysis     Bug 分析（企业通用）
├── /proj-code-review 代码审查-项目定制版
└── ...
```

### 6.6 项目仓库内置技能

技能可以随项目代码仓库下发：

```
project-repo/
└── .skills/
    ├── debug-helper.md      # Markdown 格式技能定义
    ├── pr-review.yaml       # YAML 格式技能定义
    └── README.md            # 技能使用说明
```

本地技能与平台技能使用同一套规范，可双向同步（本地 → 上传至平台 / 平台 → 下载到本地）。

---

## 7. 工具集模块

### 7.1 模块定位

工具集是平台的执行能力层，AI 在对话中可以调用这些工具完成实际操作，如查询数据库、发起 HTTP 请求、操作代码仓库等。

对用户呈现为"工具"，技术层面基于 Function Calling（Function Tools）实现，通过 MCP Tools 类型暴露。

### 7.2 工具分层

```
工具层级
├── 平台内置工具（不可删除）
│   ├── search_knowledge      搜索企业/项目知识库
│   ├── list_skills           列出可用技能
│   └── get_project_info      获取当前项目信息
│
├── 企业级工具（平台管理员管理）
│   ├── 内部系统集成
│   │   ├── query_jira        查询 Jira 工单
│   │   ├── create_jira       创建 Jira 工单
│   │   ├── query_gitlab      查询 GitLab 信息
│   │   └── send_notification 发送企业通知
│   └── 数据工具
│       └── query_data_platform 查询数据平台
│
└── 项目级工具（项目管理员管理）
    ├── 项目特定 API 调用
    ├── 项目数据库查询
    └── 自定义业务工具
```

### 7.3 工具定义规范

```jsonc
{
  "name": "search_knowledge",
  "display_name": "搜索知识库",
  "description": "在企业知识库和项目知识库中进行语义搜索",
  "scope": "global",
  "category": "knowledge",
  "input_schema": {
    "type": "object",
    "properties": {
      "query": {
        "type": "string",
        "description": "搜索关键词或问题"
      },
      "scope": {
        "type": "string",
        "enum": ["global", "project", "all"],
        "default": "all",
        "description": "搜索范围"
      },
      "top_k": {
        "type": "integer",
        "default": 5,
        "description": "返回结果数量"
      }
    },
    "required": ["query"]
  },
  "permission_required": "knowledge.read",
  "audit_level": "normal"        // normal | sensitive | critical
}
```

### 7.4 工具注册中心

平台维护一个**工具注册中心**，记录所有可用工具：

| 字段 | 说明 |
|------|------|
| 工具 ID | 唯一标识 |
| 名称 | 展示名称 |
| 描述 | AI 理解用途的描述（影响 AI 调用决策） |
| 分类 | 知识 / 代码 / 项目管理 / 通知 / 数据 / 自定义 |
| 所属范围 | 全局 / 项目 |
| 所需权限 | 调用此工具需要的权限点 |
| 审计等级 | 是否记录调用参数和结果 |
| 实现方式 | 内部函数 / HTTP 回调 / MCP 代理 |
| 状态 | 启用 / 禁用 |

### 7.5 工具注入策略

代理微服务在转发请求时，根据以下规则决定注入哪些工具：

```
注入规则（优先级从高到低）：
1. 成员当前项目的项目级工具
2. 成员所在工作区角色有权限的企业级工具
3. 平台内置工具（始终注入）

过滤规则：
- 成员无权限的工具不注入
- 当前对话上下文不相关的工具可按配置省略（降低 Token 消耗）
- 工具总数超过上限（默认 20 个）时，按优先级截断
```

---

## 8. 企业级原子能力模块

### 8.1 模块定位

原子能力是平台的**能力沉淀层**，将企业在 AI 应用实践中形成的标准化底层能力文档化、规范化，供项目团队和 Agent 快速复用，避免重复建设。

**原子能力 ≠ 工具**：原子能力是标准 + 接口规范 + 最佳实践文档，工具是可执行的代码。

### 8.2 原子能力分类

```
原子能力目录
├── 模型调用规范
│   ├── 统一 API 调用规范
│   ├── Prompt 工程最佳实践
│   ├── 流式响应处理规范
│   └── 错误处理与重试规范
│
├── 知识检索能力
│   ├── RAG 标准接入接口
│   ├── 混合检索策略规范
│   └── 知识库更新推送规范
│
├── Agent 能力
│   ├── Agent 设计模式（ReAct / Plan-Execute / Multi-Agent）
│   ├── Memory 管理规范
│   ├── Tool Calling 最佳实践
│   └── Agent 安全边界规范
│
├── MCP 开发规范
│   ├── 自建 MCP 服务器开发指南
│   ├── MCP Tool 设计规范
│   └── MCP 接入平台网关流程
│
└── 数据处理能力
    ├── 文档解析标准接口
    ├── 结构化数据处理规范
    └── 多模态输入处理规范
```

### 8.3 原子能力页面结构

```
平台管理 → 原子能力
├── 能力目录（树形导航）
├── 能力详情页
│   ├── 能力描述
│   ├── 接入文档（Markdown，含代码示例）
│   ├── API 规范（OpenAPI 格式）
│   ├── SDK 示例（多语言）
│   └── 相关技能和工具推荐
└── 能力订阅
    └── 项目可订阅，获取更新通知
```

### 8.4 标准接入文档模板

每个原子能力必须提供以下文档：

```markdown
## 能力名称

### 一句话描述
### 适用场景
### 快速开始（5 分钟示例）
### 接口规范
### 参数说明
### 错误码
### 最佳实践
### 常见问题
### 更新日志
```

---

## 9. MCP 集成模块

### 9.1 模块定位

MCP 集成模块管理所有 MCP 服务器的接入，包括：
- 平台自建的 MCP 服务器（知识库、技能、工具）
- 第三方公开 MCP 服务器（GitHub、Slack 等）
- 企业内部系统的 MCP 服务器（自研）

对用户呈现为"**集成**"，不暴露 MCP 技术术语。

### 9.2 MCP 服务器分类

| 分类 | 来源 | 管理者 | 示例 |
|------|------|--------|------|
| 平台内置 | 平台自建 | 不可删除 | 知识库 MCP、技能 MCP |
| 官方精选 | 知名开源 MCP | 平台管理员维护 | GitHub、Slack、PostgreSQL |
| 企业自建 | 内部开发 | 平台管理员 | 内部系统集成 |
| 项目自建 | 项目团队开发 | 项目管理员 | 项目特定集成 |

### 9.3 集成市场功能需求

```
集成市场
├── 浏览与搜索
│   ├── 分类筛选（代码 / 项目管理 / 数据库 / 通知 / 文件 ...）
│   ├── 关键词搜索
│   └── 推荐排行
│
├── 集成详情
│   ├── 功能描述
│   ├── 提供的 Tools/Resources/Prompts 列表
│   ├── 所需权限说明
│   ├── 配置参数说明
│   └── 使用示例
│
└── 集成管理
    ├── 一键启用（OAuth 授权 或 Token 配置）
    ├── 连通性测试
    ├── 启用 / 禁用
    └── 使用统计
```

### 9.4 平台 MCP 网关规范

平台 MCP 网关作为统一入口，对外暴露所有平台能力：

```
端点设计：
GET  /mcp/global                    企业全局能力（知识库+全局技能+全局工具）
GET  /mcp/project/{projectId}       项目能力（含继承全局）
GET  /mcp/third/{serverName}        第三方 MCP 代理

认证：
Authorization: Bearer {platform_credential}

能力发现（MCP 标准）：
{
  "tools": [...],         // 所有可用 Function Tools
  "resources": [...],     // 所有可读知识库资源
  "prompts": [...]        // 所有可用技能模板
}
```

### 9.5 第三方 MCP 接入流程

```
接入方式 A：OAuth 授权（适用 GitHub/Slack 等）
  管理员点击启用 → OAuth 授权 → 平台保存 Token → 自动代理请求

接入方式 B：Token 配置（适用自建 MCP）
  管理员填写 MCP 服务器地址 + API Key
  → 平台测试连通性 → 启用 → 注册至网关路由表

接入方式 C：本地 MCP（随项目代码）
  项目 .tools/ 目录内的本地 MCP Server
  → 成员本地启动 → 在 .claude/settings.json 中注册
```

### 9.6 自建 MCP 开发规范

企业内部团队自建 MCP 服务器时，平台提供标准规范和脚手架：

```
平台 MCP 开发包（SDK）
├── 脚手架生成器
│   └── npx create-platform-mcp my-mcp-server
│
├── 标准工具定义模板
├── 认证中间件（验证平台凭证）
├── 日志 & 审计中间件
└── 发布到平台流程文档
```

---

## 10. 代理微服务设计

### 10.1 技术选型

| 维度 | 选型 | 理由 |
|------|------|------|
| 语言/运行时 | Node.js (TypeScript) | 流式响应处理原生支持，生态成熟 |
| HTTP 框架 | Fastify | 高性能，低延迟 |
| 部署 | Docker + 独立服务 | 与现有 Spring Boot 解耦，独立扩缩容 |

### 10.2 核心处理流程

```
请求到达代理
    ↓
① 解析 Authorization Header，提取平台凭证
    ↓
② 调用后端 /internal/auth 验证凭证有效性，获取：
   - 成员 ID、项目列表、角色权限
   - 可用工具列表（已过滤权限）
   - System Prompt 片段（知识摘要 + 项目规范）
    ↓
③ 改写请求体：
   - 替换 Authorization 为真实上游 API Key
   - 注入 system 消息（平台上下文）
   - 注入 tools 列表
    ↓
④ 转发至上游 AI API，流式透传响应
    ↓
⑤ 在流式响应中拦截 tool_call 事件：
   - 路由至平台 MCP 网关执行
   - 将执行结果追加为 tool_result 消息
   - 继续请求上游 AI（多轮 tool_call 支持）
    ↓
⑥ 响应结束后异步写入：
   - Token 用量记录
   - 审计日志
   - 配额扣减
```

### 10.3 性能要求

| 指标 | 要求 |
|------|------|
| 代理增加的延迟（首 token） | ≤ 200ms P99 |
| 吞吐量 | ≥ 500 并发请求 |
| 可用性 | 99.9% |
| Tool Call 执行超时 | 默认 10s，可配置 |

---

## 11. 平台 MCP 统一网关设计

### 11.1 技术选型

与代理微服务合并部署或独立部署均可，推荐初期合并：

- 语言：Node.js (TypeScript)
- MCP SDK：`@modelcontextprotocol/sdk`
- 传输：HTTP/SSE（支持远程客户端）

### 11.2 能力路由

```typescript
// 伪代码：MCP 网关路由逻辑
router.get('/mcp/:scope', async (req, res) => {
  const { scope } = req.params  // 'global' | 'project/proj_123'
  const member = await auth(req.headers.authorization)

  return {
    tools: await loadTools(scope, member.permissions),
    resources: await loadKnowledgeResources(scope, member.projectId),
    prompts: await loadSkills(scope, member.projectId)
  }
})
```

### 11.3 Tool 执行路由

```
tool_call: search_knowledge(query="xxx")
    ↓
网关解析 tool name → 查工具注册表
    ↓
路由至对应执行器：
├── 内置工具 → 直接执行（知识库查询、项目信息等）
├── 企业工具 → 调用内部 API
├── 第三方 MCP → 代理至对应第三方服务
└── 项目工具 → 调用项目级 API
    ↓
返回结果
```

---

## 12. 产品信息架构

### 12.1 导航结构

```
平台导航
│
├── 我的工作台（成员视图）
│   ├── 我的凭证
│   │   ├── 凭证状态 & 详情
│   │   ├── 接入指南（分工具）
│   │   └── 接入测试
│   ├── 我的能力
│   │   ├── 可用知识库（继承全局 + 项目）
│   │   ├── 可用技能（继承全局 + 项目）
│   │   └── 可用工具（继承全局 + 项目）
│   └── 我的用量
│       ├── Token 消耗趋势
│       ├── 按模型/工具/技能分布
│       └── 配额剩余
│
├── 项目（项目管理员视图）
│   ├── AI 能力（核心模块）
│   │   ├── 知识库 Tab
│   │   │   ├── 继承自企业的知识库
│   │   │   └── 项目自有知识库（上传/管理）
│   │   ├── 技能库 Tab
│   │   │   ├── 继承自企业的技能
│   │   │   └── 项目自有技能（创建/编辑）
│   │   ├── 工具集 Tab
│   │   │   ├── 继承自企业的工具
│   │   │   └── 项目自有工具（注册/配置）
│   │   └── 集成 Tab
│   │       ├── 已接入的集成（MCP 服务器）
│   │       └── 集成市场入口
│   ├── 成员管理
│   └── 配额管理
│
└── 平台管理（平台管理员视图）
    ├── 企业 AI 资源
    │   ├── 上游凭证管理（真实 API Key）
    │   ├── 模型路由配置
    │   └── 全局配额策略
    ├── AI 能力资产
    │   ├── 全局知识库管理
    │   ├── 全局技能管理
    │   ├── 全局工具管理
    │   └── 集成市场管理
    ├── 原子能力中心
    │   ├── 能力目录管理
    │   └── 标准文档发布
    └── 运营监控
        ├── 全平台用量看板
        ├── 审计日志
        └── 告警配置
```

---

## 13. 核心用户流程

### 13.1 成员首次接入

```
前置条件：管理员已将成员加入项目工作区

Step 1  成员登录平台 → 进入"我的工作台"
Step 2  看到已分配的工作区和能力概览
Step 3  点击"接入指南"→ 选择工具（Claude Code / Cursor / Codex）
Step 4  平台展示接入材料 + 一键安装命令
Step 5  成员执行安装命令（约 30 秒）
Step 6  平台自动检测连通性 → 显示"接入成功"
Step 7  成员在工具中输入 / 看到可用技能列表
```

### 13.2 成员使用技能

```
成员在 Claude Code 中输入 /code-review

→ MCP 返回技能 Prompt 模板
→ 自动注入：项目编码规范（知识库）+ 项目架构摘要
→ AI 收到完整上下文
→ AI 进行代码审查，如需要自动调用 search_knowledge
→ AI 返回结构化审查报告
→ 代理层记录：用户 / 项目 / 技能 / Token 消耗
```

### 13.3 项目管理员配置知识库

```
Step 1  进入项目 → AI 能力 → 知识库 Tab
Step 2  点击"上传文档"→ 拖拽 PDF/Word/Markdown 文件
Step 3  设置文档分类、访问权限
Step 4  系统后台处理（解析 → 向量化）→ 完成通知
Step 5  点击"检索测试"→ 输入 Query → 验证效果
Step 6  配置"System Prompt 摘要"（可选，控制哪些内容自动注入）
```

### 13.4 项目管理员创建项目技能

```
Step 1  进入项目 → AI 能力 → 技能库 Tab → 新建技能
Step 2  填写技能名称 / 描述 / 触发命令
Step 3  编写 Prompt 模板（支持变量语法）
Step 4  关联知识库（选择注入哪些文档）
Step 5  绑定工具（选择该技能可调用的工具）
Step 6  点击"测试运行"→ 验证效果
Step 7  发布 → 项目成员立即可在客户端使用
```

### 13.5 项目管理员接入第三方集成

```
Step 1  进入项目 → AI 能力 → 集成 Tab → 浏览集成市场
Step 2  选择 GitHub 集成 → 点击"接入"
Step 3  OAuth 授权 GitHub → 平台获取 Token
Step 4  配置权限范围（只读 / 读写）
Step 5  点击"测试连通"→ 成功
Step 6  GitHub 相关工具（查 Issue / PR / 代码）自动对项目成员可用
```

---

## 14. 数据模型

### 14.1 凭证相关

```sql
-- 平台凭证
CREATE TABLE platform_credentials (
  id            BIGINT PRIMARY KEY,
  key_hash      VARCHAR(64) UNIQUE NOT NULL,   -- SHA256(key)，不存明文
  key_prefix    VARCHAR(16) NOT NULL,           -- plt_uid_xxx，用于展示
  member_id     BIGINT NOT NULL,
  credential_type ENUM('personal', 'service_account', 'temporary'),
  status        ENUM('active', 'revoked', 'expired'),
  expires_at    DATETIME,
  last_used_at  DATETIME,
  created_at    DATETIME,
  revoked_at    DATETIME,
  revoke_reason VARCHAR(256)
);
```

### 14.2 知识库相关

```sql
-- 知识库
CREATE TABLE knowledge_bases (
  id          BIGINT PRIMARY KEY,
  name        VARCHAR(128),
  scope       ENUM('global', 'project'),
  project_id  BIGINT,                          -- NULL if global
  status      ENUM('active', 'indexing', 'error'),
  doc_count   INT DEFAULT 0,
  created_at  DATETIME
);

-- 知识库文档
CREATE TABLE kb_documents (
  id              BIGINT PRIMARY KEY,
  kb_id           BIGINT NOT NULL,
  title           VARCHAR(256),
  file_type       VARCHAR(32),
  file_size       BIGINT,
  chunk_count     INT,
  embedding_model VARCHAR(64),
  status          ENUM('pending', 'processing', 'ready', 'error'),
  created_at      DATETIME,
  updated_at      DATETIME
);
```

### 14.3 技能相关

```sql
-- 技能定义
CREATE TABLE skills (
  id              BIGINT PRIMARY KEY,
  skill_key       VARCHAR(64) UNIQUE,           -- 如 code-review
  name            VARCHAR(128),
  description     TEXT,
  scope           ENUM('global', 'project'),
  project_id      BIGINT,
  system_prompt   TEXT,
  knowledge_refs  JSON,                          -- 关联知识库
  bound_tools     JSON,                          -- 绑定工具列表
  parameters      JSON,                          -- 用户可配参数
  version         VARCHAR(16),
  status          ENUM('draft', 'published', 'deprecated'),
  usage_count     BIGINT DEFAULT 0,
  created_by      BIGINT,
  created_at      DATETIME,
  published_at    DATETIME
);
```

### 14.4 工具相关

```sql
-- 工具注册表
CREATE TABLE tools (
  id              BIGINT PRIMARY KEY,
  tool_name       VARCHAR(64) UNIQUE,
  display_name    VARCHAR(128),
  description     TEXT,
  scope           ENUM('builtin', 'global', 'project'),
  project_id      BIGINT,
  category        VARCHAR(32),
  input_schema    JSON,
  impl_type       ENUM('internal', 'http_callback', 'mcp_proxy'),
  impl_config     JSON,                          -- 实现配置（URL等）
  permission_required VARCHAR(64),
  audit_level     ENUM('normal', 'sensitive', 'critical'),
  status          ENUM('active', 'disabled'),
  created_at      DATETIME
);
```

### 14.5 MCP 集成相关

```sql
-- MCP 服务器注册
CREATE TABLE mcp_servers (
  id              BIGINT PRIMARY KEY,
  server_name     VARCHAR(64) UNIQUE,
  display_name    VARCHAR(128),
  description     TEXT,
  server_type     ENUM('builtin', 'official', 'enterprise', 'project'),
  project_id      BIGINT,
  server_url      VARCHAR(256),
  auth_type       ENUM('none', 'bearer', 'oauth2'),
  auth_config     JSON,                          -- 加密存储
  capabilities    JSON,                          -- 已发现的 tools/resources/prompts
  status          ENUM('active', 'disabled', 'error'),
  last_checked_at DATETIME,
  created_at      DATETIME
);
```

---

## 15. 接口规范

### 15.1 代理微服务接口

```
POST /proxy/anthropic/v1/messages
POST /proxy/anthropic/v1/messages  (stream)
POST /proxy/openai/v1/chat/completions
POST /proxy/openai/v1/chat/completions (stream)

认证：Authorization: Bearer {platform_credential}
行为：
  - 验证凭证 → 注入能力 → 转发 → 追踪用量
  - 完全兼容上游 API 格式，客户端无感知
```

### 15.2 平台 MCP 网关接口

```
GET  /mcp/global                    获取全局能力列表（MCP 标准格式）
GET  /mcp/project/{projectId}       获取项目能力列表（含继承全局）
POST /mcp/global/tools/call         执行全局工具
POST /mcp/project/{projectId}/tools/call  执行项目工具
GET  /mcp/third/{serverName}        代理第三方 MCP 发现

认证：Authorization: Bearer {platform_credential}
协议：MCP over HTTP/SSE（JSON-RPC 2.0）
```

### 15.3 管理 API

```
# 知识库
POST   /api/knowledge-bases                  创建知识库
GET    /api/knowledge-bases/{id}             获取知识库详情
POST   /api/knowledge-bases/{id}/documents  上传文档
DELETE /api/knowledge-bases/{id}/documents/{docId}
POST   /api/knowledge-bases/{id}/search     检索测试

# 技能库
POST   /api/skills                           创建技能
GET    /api/skills?scope={global|project}   列出技能
PUT    /api/skills/{id}                      更新技能
POST   /api/skills/{id}/publish             发布技能
POST   /api/skills/{id}/test                测试技能

# 工具集
POST   /api/tools                            注册工具
GET    /api/tools?scope={global|project}    列出工具
PUT    /api/tools/{id}/status               启用/禁用工具

# MCP 集成
GET    /api/mcp-servers                      列出 MCP 服务器
POST   /api/mcp-servers                      注册 MCP 服务器
POST   /api/mcp-servers/{id}/test           测试连通性
DELETE /api/mcp-servers/{id}               删除集成

# 凭证管理
GET    /api/credentials/mine               获取我的凭证
POST   /api/credentials/mine/rotate        轮换凭证
GET    /api/credentials/mine/setup-script  获取一键安装脚本
POST   /api/credentials/mine/test          测试凭证连通性
DELETE /api/credentials/{id}/revoke       吊销凭证（管理员）
```

---

## 16. 非功能性需求

### 16.1 安全

| 要求 | 描述 |
|------|------|
| 凭证安全 | 真实上游 API Key 只存后端，加密存储，不下发给成员 |
| 凭证传输 | 全程 HTTPS，凭证不写入日志 |
| 权限隔离 | 项目 A 的知识库/技能不可被项目 B 成员访问 |
| 工具调用审计 | 敏感工具调用记录参数和结果 |
| 最小权限 | 默认不注入成员无权限的工具 |

### 16.2 可用性与性能

| 要求 | 目标 |
|------|------|
| 代理服务可用性 | 99.9%（月） |
| 代理延迟增量 | ≤ 200ms P99（首 token） |
| 知识库检索延迟 | ≤ 500ms P95 |
| MCP 能力发现延迟 | ≤ 100ms（缓存命中）|
| 文档向量化处理 | 异步，完成后通知 |

### 16.3 扩展性

| 要求 | 描述 |
|------|------|
| 新增 AI 客户端 | 代理层支持新增适配器，不影响现有功能 |
| 新增 MCP 服务器 | 注册即可用，无需修改代码 |
| 新增上游 AI 供应商 | 代理层路由可扩展，支持 Gemini / DeepSeek 等 |
| 知识库后端可替换 | 检索接口标准化，pgvector 可替换为 Milvus 等 |

---

## 17. 开发优先级与里程碑

### Phase 1 — MVP（核心接入链路）

**目标**：成员可通过平台凭证使用 Claude Code，平台能追踪用量。

```
后端（Sprint 1-2）
├── 凭证生成与验证 API
├── 代理微服务（基础版：验证 + 转发 + 用量记录）
└── 接入指南页面（凭证展示 + 安装命令生成）

后端（Sprint 3-4）
├── 平台 MCP 网关（基础版：内置 Tools + 全局知识库检索）
├── 知识库服务（文档上传 + 向量化 + 检索）
└── 技能库基础功能（CRUD + MCP Prompts 暴露）

前端
├── 我的工作台 → 我的凭证
├── 接入指南页面
└── 项目 AI 能力 Tab（知识库管理）
```

交付物：成员 3 分钟接入 Claude Code，可使用企业知识库检索。

---

### Phase 2 — 能力完善

**目标**：技能库、工具集、第三方集成可用。

```
├── 技能创建 / 编辑 / 发布完整流程
├── 工具注册中心 + 权限过滤注入
├── 集成市场（GitHub / Slack 等官方 MCP）
├── Cursor / Codex 接入支持
├── 配额管理与超限控制
└── 项目仓库内置技能支持（.skills/ 目录）
```

---

### Phase 3 — 企业治理

**目标**：原子能力沉淀，完整审计，企业级管控。

```
├── 原子能力中心（文档站）
├── 完整审计日志
├── 告警配置
├── 服务账号体系
├── 自建 MCP 开发规范 + 脚手架
└── 企业级知识库高级功能（代码库索引 / 自动更新）
```

---

*文档结束*
