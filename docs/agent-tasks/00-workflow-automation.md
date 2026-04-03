# 4-Agent 自动化执行工作流

## 新增内容

1. GitHub Actions 工作流：
   - `.github/workflows/agent-delivery-pipeline.yml`
2. 本地一键脚本（Windows PowerShell）：
   - `tools/run-agent-workflow.ps1`

## 执行顺序（自动编排）

- Phase 1: Agent B（RBAC 基础）
- Phase 2: Agent A（个人工作台/凭证） + Agent C（AI 能力配置）
- Phase 3: Agent D（网关/配额/用量）
- Final: `mvn verify`

说明：A 和 C 依赖 B 的权限基线；D 依赖 A 和 C 的接口输出。

## 本地执行

在仓库根目录运行：

```powershell
# 全流程
powershell -ExecutionPolicy Bypass -File .\tools\run-agent-workflow.ps1 -Stage ALL

# 只跑某个阶段（示例）
powershell -ExecutionPolicy Bypass -File .\tools\run-agent-workflow.ps1 -Stage B
powershell -ExecutionPolicy Bypass -File .\tools\run-agent-workflow.ps1 -Stage D
```

## CI 执行

推 PR 到 `main/master` 会自动触发；也可以在 Actions 页面手动触发 `agent-delivery-pipeline`。

## 建议分支策略

- Agent A: `feat/agent-a-me-credential`
- Agent B: `feat/agent-b-rbac`
- Agent C: `feat/agent-c-ai-config`
- Agent D: `feat/agent-d-gateway-quota`

按顺序合并：B -> A/C -> D。
