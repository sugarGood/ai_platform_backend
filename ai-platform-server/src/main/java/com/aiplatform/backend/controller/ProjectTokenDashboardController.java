package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.ProjectConsumptionByUserRowResponse;
import com.aiplatform.backend.dto.ProjectMemberAiAccessRequest;
import com.aiplatform.backend.dto.ProjectMemberQuotaRowResponse;
import com.aiplatform.backend.dto.ProjectTokenDashboardConfigResponse;
import com.aiplatform.backend.dto.ProjectTokenDashboardSummaryResponse;
import com.aiplatform.backend.dto.ProjectUsageActivityRowResponse;
import com.aiplatform.backend.dto.TokenDashboardBatchSettleRequest;
import com.aiplatform.backend.service.ProjectTokenDashboardService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 项目 Token 管理仪表盘 API（对齐运营后台：卡片、配置、成员配额、消耗分布、活跃日志）。
 *
 * <p>配置持久化沿用 {@code PUT /api/projects/{id}}，请求体使用 {@link com.aiplatform.backend.dto.UpdateProjectRequest}
 *（含 {@code quotaResetCycle}、{@code singleRequestTokenCap} 等字段）。</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/token-dashboard")
public class ProjectTokenDashboardController {

    private final ProjectTokenDashboardService projectTokenDashboardService;

    public ProjectTokenDashboardController(ProjectTokenDashboardService projectTokenDashboardService) {
        this.projectTokenDashboardService = projectTokenDashboardService;
    }

    /** 顶部卡片：Token、成员权限、日报占位、告警统计等。 */
    @GetMapping("/summary")
    public ProjectTokenDashboardSummaryResponse summary(@PathVariable Long projectId) {
        return projectTokenDashboardService.summary(projectId);
    }

    /** 项目/配置区块只读数据（保存请调 {@code PUT /api/projects/{projectId}}）。 */
    @GetMapping("/config")
    public ProjectTokenDashboardConfigResponse config(@PathVariable Long projectId) {
        return projectTokenDashboardService.config(projectId);
    }

    /** 成员配额分配表。 */
    @GetMapping("/members/quota-rows")
    public List<ProjectMemberQuotaRowResponse> memberQuotaRows(@PathVariable Long projectId) {
        return projectTokenDashboardService.memberQuotaRows(projectId);
    }

    /** 按成员汇总当月消耗（条形图/排行）。 */
    @GetMapping("/consumption-by-user")
    public List<ProjectConsumptionByUserRowResponse> consumptionByUser(@PathVariable Long projectId) {
        return projectTokenDashboardService.consumptionByUser(projectId);
    }

    /**
     * 活跃日志（分页 + 过滤）。
     *
     * @param sourceType      用量来源，如 PLATFORM_GATEWAY、MCP_TOOL
     * @param status          调用状态，如 SUCCESS、BLOCKED_BY_QUOTA
     * @param occurredAfter   起始时间（含）
     * @param occurredBefore  结束时间（含）
     */
    @GetMapping("/activity")
    public PageResponse<ProjectUsageActivityRowResponse> activity(
            @PathVariable Long projectId,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime occurredAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime occurredBefore,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return projectTokenDashboardService.activityLog(
                projectId, sourceType, status, occurredAfter, occurredBefore, page, size);
    }

    /** 为缺少 TOKEN_QUOTA 的成员按角色补全 {@code member_ai_quotas} 行。 */
    @PostMapping("/members/sync-quotas")
    public Map<String, Object> syncQuotas(@PathVariable Long projectId) {
        int created = projectTokenDashboardService.syncMemberQuotas(projectId);
        return Map.of("createdRows", created);
    }

    /** 批量清算：清零补充层已用量与/或个人池当月已用量。 */
    @PostMapping("/members/batch-settle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void batchSettle(@PathVariable Long projectId,
                            @RequestBody(required = false) TokenDashboardBatchSettleRequest body) {
        TokenDashboardBatchSettleRequest req = body != null ? body
                : new TokenDashboardBatchSettleRequest(false, false);
        projectTokenDashboardService.batchSettle(projectId, req);
    }

    /** 开启/关闭成员 AI 调用（平台凭证 ACTIVE ↔ DISABLED）。 */
    @PatchMapping("/members/{memberId}/ai-access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchAiAccess(@PathVariable Long projectId,
                              @PathVariable Long memberId,
                              @Valid @RequestBody ProjectMemberAiAccessRequest request) {
        projectTokenDashboardService.setMemberAiAccess(projectId, memberId, request);
    }
}
