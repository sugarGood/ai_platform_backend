package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.AiUsageEventQuery;
import com.aiplatform.backend.dto.AiUsageEventResponse;
import com.aiplatform.backend.dto.CreateMemberAiQuotaRequest;
import com.aiplatform.backend.dto.MemberAiQuotaResponse;
import com.aiplatform.backend.dto.MemberProjectQuotaUpsertRequest;
import com.aiplatform.backend.service.AiUsageService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class AiUsageController {

    private final AiUsageService aiUsageService;

    public AiUsageController(AiUsageService aiUsageService) {
        this.aiUsageService = aiUsageService;
    }

    @GetMapping("/api/usage-events")
    public PageResponse<AiUsageEventResponse> list(@RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) Long projectId,
                                                   @RequestParam(required = false) String sourceType,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime occurredAfter,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime occurredBefore,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return aiUsageService.listUsageEvents(
                new AiUsageEventQuery(userId, projectId, sourceType, status, occurredAfter, occurredBefore, page, size)
        );
    }

    @GetMapping("/api/me/usage/legacy")
    public Map<String, Object> myUsageSummary(@RequestParam Long userId) {
        return aiUsageService.myUsageSummary(userId);
    }

    @GetMapping("/api/me/usage/events/legacy")
    public PageResponse<AiUsageEventResponse> myUsageEvents(@RequestParam Long userId,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return aiUsageService.listUsageEvents(
                new AiUsageEventQuery(userId, null, null, null, null, null, page, size)
        );
    }

    @GetMapping("/api/projects/{projectId}/usage/summary")
    public Map<String, Object> projectUsageSummary(@PathVariable Long projectId) {
        return aiUsageService.projectUsageSummary(projectId);
    }

    @GetMapping("/api/projects/{projectId}/usage/events")
    public PageResponse<AiUsageEventResponse> projectUsageEvents(@PathVariable Long projectId,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return aiUsageService.listUsageEvents(
                new AiUsageEventQuery(null, projectId, null, null, null, null, page, size)
        );
    }

    @GetMapping("/api/admin/usage/dashboard")
    public Map<String, Object> platformDashboard() {
        return aiUsageService.platformDashboard();
    }

    @GetMapping("/api/projects/{projectId}/member-quotas")
    public List<MemberAiQuotaResponse> projectMemberQuotas(@PathVariable Long projectId) {
        return aiUsageService.listQuotasByProjectId(projectId).stream()
                .map(MemberAiQuotaResponse::from)
                .toList();
    }

    @PostMapping("/api/projects/{projectId}/members/{memberId}/quota")
    public MemberAiQuotaResponse setMemberQuota(@PathVariable Long projectId,
                                                @PathVariable Long memberId,
                                                @Valid @RequestBody MemberProjectQuotaUpsertRequest request) {
        return MemberAiQuotaResponse.from(
                aiUsageService.upsertQuotaForProjectMember(projectId, memberId, request)
        );
    }

    @PutMapping("/api/member-quotas/{quotaId}")
    public MemberAiQuotaResponse updateQuota(@PathVariable Long quotaId,
                                             @RequestBody CreateMemberAiQuotaRequest request) {
        return MemberAiQuotaResponse.from(aiUsageService.updateQuota(quotaId, request));
    }
}
