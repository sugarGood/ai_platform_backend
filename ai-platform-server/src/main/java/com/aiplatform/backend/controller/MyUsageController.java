package com.aiplatform.backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.MyUsageEventResponse;
import com.aiplatform.backend.dto.MyUsageSummaryResponse;
import com.aiplatform.backend.dto.UsageProjectDistributionRowResponse;
import com.aiplatform.backend.dto.UsageTrendPointResponse;
import com.aiplatform.backend.service.MyUsageService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 我的用量接口。
 */
@Validated
@RestController
@RequestMapping("/api/me/usage")
public class MyUsageController {

    private final MyUsageService myUsageService;

    public MyUsageController(MyUsageService myUsageService) {
        this.myUsageService = myUsageService;
    }

    /**
     * 获取本月个人用量总览。
     */
    @GetMapping("/summary")
    public MyUsageSummaryResponse summary() {
        return myUsageService.getSummary(currentUserId());
    }

    /**
     * 获取个人用量趋势。
     */
    @GetMapping("/trend")
    public List<UsageTrendPointResponse> trend(
            @RequestParam(name = "days", defaultValue = "7") @Min(1) @Max(90) Integer days) {
        return myUsageService.getTrend(currentUserId(), days);
    }

    /**
     * 获取请求级明细。
     */
    @GetMapping("/events")
    public PageResponse<MyUsageEventResponse> events(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(200) Integer size) {
        return myUsageService.getEvents(currentUserId(), page, size);
    }

    /**
     * 获取按项目分布。
     */
    @GetMapping("/projects")
    public List<UsageProjectDistributionRowResponse> projects() {
        return myUsageService.getProjectDistribution(currentUserId());
    }

    private Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
