# Agent module v2

当前按 `docs/db/20260319_workspace_ai_schema.mysql.sql` 的职责边界重新划分：
- `ai-platform-server`：承载项目、成员、服务、AI Key，以及工作区/能力目录/工作区能力绑定等管理接口。
- `ai-platform-agent`：作为真正的 workspace-oriented AI gateway，对外暴露运行时 AI 代理入口。

## `ai-platform-server` 当前负责
- `project_workspaces`
- `ai_capabilities`
- `workspace_ai_capabilities`
- 以及原有的 `projects` / `project_members` / `services` / `project_ai_keys`

当前管理接口包括：
- `POST /api/projects/{projectId}/workspaces`
- `GET /api/projects/{projectId}/workspaces`
- `GET /api/agent/capabilities`
- `POST /api/workspaces/{workspaceId}/capabilities`
- `GET /api/workspaces/{workspaceId}/capabilities`

## `ai-platform-agent` 当前负责
- `POST /api/gateway/workspaces/{workspaceId}/chat/completions`
- 校验工作区是否存在且启用
- 校验能力目录是否存在且已绑定到工作区
- 基于项目级 `project_ai_keys` 选择上游 provider key
- 代理 OpenAI-compatible chat completions 请求
- 成功请求后递增项目 AI Key 的 `used_quota`

## 后续可继续补齐
- `project_workspace_members`
- `workspace_ai_credentials`
- `workspace_member_ai_credentials`
- usage / audit / access token 维度的联动落表
- 流式转发、provider failover、workspace/member 级额度控制
