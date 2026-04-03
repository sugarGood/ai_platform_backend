# Agent D 任务卡（双池配额 + 网关主链路 + 联调）

## 目标
打通运行面主链路：凭证解析 -> 项目上下文 -> 能力装配 -> 配额检查/扣减 -> 用量落库。

## 必做范围
1. 双池配额（项目池 + 成员池）check/commit 两阶段。
2. 用量接口：
   - `GET /api/me/usage/summary`
   - `GET /api/me/usage/trend`
   - `GET /api/me/usage/events`
   - `GET /api/me/usage/projects`
3. 网关至少完成 1 条 OpenAI 兼容 happy path。

## 参考文档
- `docs/01-backend-overview.md`
- `docs/03-backend-api-spec.md`
- `docs/04-prototype-development-mapping.md`
- `docs/db/ai_platform.sql`

## 约束
1. 不直接信任前端项目参数，必须二次校验项目访问权。
2. 配额失败返回明确原因（项目池不足 / 成员池不足）。
3. 用量事件需可追踪 `request_id`。
4. 可先 mock 能力装配，再接入真实实现。

## 建议改动文件
- `ai-platform-agent/src/main/java/com/aiplatform/agent/gateway/**`
- `ai-platform-server/src/main/java/com/aiplatform/backend/controller/AiUsageController.java`
- `ai-platform-server/src/main/java/com/aiplatform/backend/service/AiUsageService.java`
- quota 相关 controller/service/mapper/entity/test

## 验收标准
1. 1 条端到端成功链路测试。
2. 2 条失败链路测试（凭证无效、配额不足）。
3. 输出网关链路时序（文字即可）与接口契约。
