# Agent C 任务卡（项目 AI 能力配置闭环）

## 目标
完成项目 AI 能力配置四块闭环：知识库、技能、工具、集成。

## 必做范围
1. 知识库：项目绑定/解绑、配置更新、检索测试路径语义对齐（必要时做兼容）。
2. 技能：项目启用/移除、发布/克隆到项目流程。
3. 工具：注册/编辑/启停/测试/调用日志接口收口。
4. 集成：项目启用/移除、授权保存、连通性测试。

## 参考文档
- `docs/03-backend-api-spec.md`
- `docs/04-prototype-development-mapping.md`
- `docs/db/ai_platform.sql`

## 约束
1. 优先补行为，不做跨模块大重命名。
2. 文档路径与现有路径不一致时，提供兼容层。
3. 返回结构对齐原型页面字段需求。

## 建议改动文件
- `ai-platform-server/src/main/java/com/aiplatform/backend/controller/KnowledgeBaseController.java`
- `ai-platform-server/src/main/java/com/aiplatform/backend/controller/SkillController.java`
- `ai-platform-server/src/main/java/com/aiplatform/backend/controller/ToolDefinitionController.java`
- `ai-platform-server/src/main/java/com/aiplatform/backend/controller/ProjectKnowledgeConfigController.java`
- `ai-platform-server/src/main/java/com/aiplatform/backend/controller/ProjectMcpIntegrationController.java`
- 对应 service/dto

## 验收标准
1. 四大模块各至少 1 条主流程测试（总计 >= 4）。
2. 提供接口映射表：`文档路径 -> 实际路径 -> 是否兼容`。
3. 输出前端可直接调用的联调接口清单。
