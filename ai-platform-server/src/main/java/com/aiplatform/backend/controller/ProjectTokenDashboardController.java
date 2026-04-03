package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.ProjectConsumptionByUserRowResponse;
import com.aiplatform.backend.dto.ProjectMemberAiAccessRequest;
import com.aiplatform.backend.dto.ProjectMemberQuotaRowResponse;
import com.aiplatform.backend.dto.ProjectTokenDashboardConfigResponse;
import com.aiplatform.backend.dto.ProjectTokenDashboardSummaryResponse;
import com.aiplatform.backend.dto.ProjectUsageActivityQuery;
import com.aiplatform.backend.dto.ProjectUsageActivityRowResponse;
import com.aiplatform.backend.dto.SyncMemberQuotasResponse;
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

@RestController
@RequestMapping("/api/projects/{projectId}/token-dashboard")
public class ProjectTokenDashboardController {

    private final ProjectTokenDashboardService projectTokenDashboardService;

    public ProjectTokenDashboardController(ProjectTokenDashboardService projectTokenDashboardService) {
        this.projectTokenDashboardService = projectTokenDashboardService;
    }

    @GetMapping("/summary")
    public ProjectTokenDashboardSummaryResponse summary(@PathVariable Long projectId) {
        return projectTokenDashboardService.summary(projectId);
    }

    @GetMapping("/config")
    public ProjectTokenDashboardConfigResponse config(@PathVariable Long projectId) {
        return projectTokenDashboardService.config(projectId);
    }

    @GetMapping("/members/quota-rows")
    public List<ProjectMemberQuotaRowResponse> memberQuotaRows(@PathVariable Long projectId) {
        return projectTokenDashboardService.memberQuotaRows(projectId);
    }

    @GetMapping("/consumption-by-user")
    public List<ProjectConsumptionByUserRowResponse> consumptionByUser(@PathVariable Long projectId) {
        return projectTokenDashboardService.consumptionByUser(projectId);
    }

    @GetMapping("/activity")
    public PageResponse<ProjectUsageActivityRowResponse> activity(@PathVariable Long projectId,
                                                                 @RequestParam(required = false) String sourceType,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime occurredAfter,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime occurredBefore,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return projectTokenDashboardService.activityLog(
                projectId,
                new ProjectUsageActivityQuery(sourceType, status, occurredAfter, occurredBefore, page, size)
        );
    }

    @PostMapping("/members/sync-quotas")
    public SyncMemberQuotasResponse syncQuotas(@PathVariable Long projectId) {
        return new SyncMemberQuotasResponse(projectTokenDashboardService.syncMemberQuotas(projectId));
    }

    @PostMapping("/members/batch-settle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void batchSettle(@PathVariable Long projectId,
                            @RequestBody(required = false) TokenDashboardBatchSettleRequest body) {
        TokenDashboardBatchSettleRequest request = body != null
                ? body
                : new TokenDashboardBatchSettleRequest(false, false);
        projectTokenDashboardService.batchSettle(projectId, request);
    }

    @PatchMapping("/members/{memberId}/ai-access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchAiAccess(@PathVariable Long projectId,
                              @PathVariable Long memberId,
                              @Valid @RequestBody ProjectMemberAiAccessRequest request) {
        projectTokenDashboardService.setMemberAiAccess(projectId, memberId, request);
    }
}
