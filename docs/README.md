# AI 能力平台后端文档目录

## 文档目标

本目录不再只做“概念说明”，而是作为可直接对照原型开发的后端设计文档。

约束口径：

1. 以原型页面和 `原型/2026-03-19-ai-capability-platform-requirements.md` 为第一依据。
2. 以 `docs/db/ai_platform.sql` 作为目标 DDL，不再把历史 SQL 当成唯一事实来源。
3. 以“页面动作 -> 接口 -> 数据表 -> 异步任务/审计”的方式组织文档，方便研发直接落实现。

## 阅读顺序

### [01-backend-overview.md](./01-backend-overview.md)

说明平台边界、核心链路、服务拆分、原型到后端模块的总映射。

### [02-backend-database-design.md](./02-backend-database-design.md)

说明目标数据模型、表职责、关键关系、业务约束，以及与现有 SQL 的收敛方案。

### [03-backend-api-spec.md](./03-backend-api-spec.md)

说明按页面和业务动作拆解后的接口清单，便于前后端联调和服务拆分。

### [04-prototype-development-mapping.md](./04-prototype-development-mapping.md)

说明“原型页面 -> 读写接口 -> 涉及表 -> 日志/异步任务”的开发对照矩阵。

## SQL

### [db/ai_platform.sql](./db/ai_platform.sql)

目标数据库 DDL，已经按原型重新整理：

- 保留 AI 能力平台主链路所需表
- 补齐项目角色模板、成员能力覆写、项目共享 Token 池、集成授权等缺口
- 删除历史 SQL 中与当前原型主链路不一致、且暂不作为首批建设目标的杂项建模

### [db/patch_01_auth_init.sql](./db/patch_01_auth_init.sql)

历史认证初始化补丁，保留作参考，不作为目标建库主线。

## 当前文档结论

本轮整理后的开发主线是：

1. 一人一证，多项目复用，项目上下文通过“当前工作项目”或请求头切换。
2. 项目权限以“角色模板 + 成员覆写 + 能力资源授权”三层模型实现。
3. 配额采用“项目共享池 + 成员个人配额”双池模型。
4. AI 能力配置页对应五类资产：知识库、技能、工具、System Prompt、集成。
5. 平台级与项目级能力均支持继承、订阅、启停、审计和用量统计。
