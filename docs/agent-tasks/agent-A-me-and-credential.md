# Agent A 任务卡（个人工作台 + 凭证域）

## 目标
落地个人工作台凭证相关接口，复用现有 service，不做大重构。

## 必做接口
- `GET /api/me/credential`
- `GET /api/me/setup-materials`
- `POST /api/me/credential/rotate`
- `POST /api/me/credential/renew`
- `POST /api/me/credential/test`
- `PUT /api/me/current-project`

## 参考文档
- `docs/03-backend-api-spec.md`
- `docs/04-prototype-development-mapping.md`
- `docs/db/ai_platform.sql`

## 约束
1. Controller 保持薄层，只做参数接收与分发。
2. 不直接返回 entity，补齐 request/response DTO。
3. 副作用必须落地：
   - rotate/renew 写 `credential_rotation_logs`
   - test 更新 `user_client_bindings.last_active_at`
   - current-project 更新 `platform_credentials.default_project_id`
   - 敏感操作写 `activity_logs`（必要时 `audit_logs`）
4. 保持 `/api/credentials/*` 可用，新增 `/api/me/*` 聚合层。

## 建议改动文件
- 新增：`ai-platform-server/src/main/java/com/aiplatform/backend/controller/MeController.java`
- 新增：`ai-platform-server/src/main/java/com/aiplatform/backend/dto/me/*`
- 修改：`ai-platform-server/src/main/java/com/aiplatform/backend/service/PlatformCredentialService.java`
- 修改：`ai-platform-server/src/main/java/com/aiplatform/backend/service/UserClientBindingService.java`

## 验收标准
1. 上述 6 个接口可通过本地联调。
2. 至少 1 个 service 单测 + 1 个 controller 集成测试。
3. 提交结果包含：变更文件列表、接口清单、测试结果。
