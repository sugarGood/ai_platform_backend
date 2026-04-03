# Agent B 任务卡（RBAC 三层权限模型）

## 目标
实现并打通“角色模板默认权限 + 成员覆写 + 资源授权收敛”。

## 必做能力
1. 项目角色模板管理：列表/新增/更新/权限矩阵。
2. 成员权限覆写保存。
3. 成员资源授权保存（知识库/技能/工具/集成）。
4. 提供能力计算逻辑：`模板 -> 覆写 -> 资源收敛`。

## 参考文档
- `docs/02-backend-database-design.md`
- `docs/03-backend-api-spec.md`
- `docs/04-prototype-development-mapping.md`
- `docs/db/ai_platform.sql`

## 约束
1. 不做大规模重构，只补齐缺口。
2. service 做业务编排，controller 做参数接收和分发。
3. 命名、状态枚举遵循现有仓库风格。
4. 结果需可被 `/api/me/abilities` 与项目 AI 能力页复用。

## 建议改动文件
- 修改：`ai-platform-server/src/main/java/com/aiplatform/backend/controller/ProjectMemberController.java`
- 新增/修改：项目角色模板相关 controller/service/dto
- 新增：`ai-platform-server/src/main/java/com/aiplatform/backend/service/PermissionAssemblyService.java`（或等价实现）
- 修改：`ai-platform-server/src/main/java/com/aiplatform/backend/service/RbacService.java`

## 验收标准
1. 覆盖至少 2 个关键场景：
   - 角色默认允许，但成员覆写拒绝。
   - 角色允许、成员允许，但资源未授权时不可见。
2. 至少 2 个单测覆盖权限合并逻辑。
3. 输出包含：规则说明、测试结果、影响接口清单。
