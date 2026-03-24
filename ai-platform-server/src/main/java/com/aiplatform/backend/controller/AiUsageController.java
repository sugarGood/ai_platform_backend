package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.AiUsageEventResponse;
import com.aiplatform.backend.dto.CreateMemberAiQuotaRequest;
import com.aiplatform.backend.dto.MemberAiQuotaResponse;
import com.aiplatform.backend.service.AiUsageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI 用量与配额控制器（完整版）。
 *
 * <p>覆盖我的用量、项目用量、平台看板、成员配额管理等接口。</p>
 */
@RestController
public class AiUsageController {

    private final AiUsageService aiUsageService;

    public AiUsageController(AiUsageService aiUsageService) {
        this.aiUsageService = aiUsageService;
    }

    // ── 用量明细 ──────────────────────────────────────────────────────

    /** 分页查询 AI 用量明细（支持 userId/projectId 过滤）。 */
    @GetMapping("/api/usage-events")
    public PageResponse<AiUsageEventResponse> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return aiUsageService.listUsageEvents(userId, projectId, page, size);
    }

    // ── 我的用量 ─────────────────────────────────────────────────────

    /** 我的用量概览（当月总量）。 */
    @GetMapping("/api/me/usage")
    public Map<String, Object> myUsageSummary(@RequestParam Long userId) {
        return aiUsageService.myUsageSummary(userId);
    }

    /** 我的用量明细（分页）。 */
    @GetMapping("/api/me/usage/events")
    public PageResponse<AiUsageEventResponse> myUsageEvents(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return aiUsageService.listUsageEvents(userId, null, page, size);
    }

    // ── 项目用量 ─────────────────────────────────────────────────────

    /** 项目用量统计摘要。 */
    @GetMapping("/api/projects/{projectId}/usage/summary")
    public Map<String, Object> projectUsageSummary(@PathVariable Long projectId) {
        return aiUsageService.projectUsageSummary(projectId);
    }

    /** 项目用量明细（分页）。 */
    @GetMapping("/api/projects/{projectId}/usage/events")
    public PageResponse<AiUsageEventResponse> projectUsageEvents(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return aiUsageService.listUsageEvents(null, projectId, page, size);
    }

    // ── 平台看板（管理员） ────────────────────────────────────────────

    /** 平台级用量看板（全平台当月汇总）。 */
    @GetMapping("/api/admin/usage/dashboard")
    public Map<String, Object> platformDashboard() {
        return aiUsageService.platformDashboard();
    }

    // ── 成员配额管理 ──────────────────────────────────────────────────

    /** 查询项目成员配额列表。 */
    @GetMapping("/api/projects/{projectId}/member-quotas")
    public List<MemberAiQuotaResponse> projectMemberQuotas(@PathVariable Long projectId) {
        return aiUsageService.listQuotasByProjectId(projectId).stream()
                .map(MemberAiQuotaResponse::from).toList();
    }

    /** 设置成员配额。 */
    @PostMapping("/api/projects/{projectId}/members/{memberId}/quota")
    public MemberAiQuotaResponse setMemberQuota(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody CreateMemberAiQuotaRequest request) {
        return MemberAiQuotaResponse.from(aiUsageService.createQuota(request));
    }

    /** 更新成员配额。 */
    @PutMapping("/api/member-quotas/{quotaId}")
    public MemberAiQuotaResponse updateQuota(
            @PathVariable Long quotaId,
            @RequestBody CreateMemberAiQuotaRequest request) {
        return MemberAiQuotaResponse.from(aiUsageService.updateQuota(quotaId, request));
    }
}
