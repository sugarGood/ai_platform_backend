package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateSkillRequest;
import com.aiplatform.backend.dto.SkillResponse;
import com.aiplatform.backend.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 技能管理控制器（完整版）。
 *
 * <p>覆盖技能全生命周期：创建、编辑、发布、审核、废弃、反馈、项目绑定/解绑。</p>
 */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    /** 创建技能。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@Valid @RequestBody CreateSkillRequest request) {
        return SkillResponse.from(skillService.create(request));
    }

    /** 查询技能列表，支持 scope 过滤。 */
    @GetMapping
    public List<SkillResponse> list(@RequestParam(required = false) String scope) {
        return skillService.list(scope).stream().map(SkillResponse::from).toList();
    }

    /** 根据ID查询技能详情。 */
    @GetMapping("/{id}")
    public SkillResponse getById(@PathVariable Long id) {
        return SkillResponse.from(skillService.getByIdOrThrow(id));
    }

    /** 编辑技能。 */
    @PutMapping("/{id}")
    public SkillResponse update(@PathVariable Long id,
                                @RequestBody CreateSkillRequest request) {
        return SkillResponse.from(skillService.update(id, request));
    }

    /** 发布技能（DRAFT → 审核中）。 */
    @PostMapping("/{id}/publish")
    public SkillResponse publish(@PathVariable Long id) {
        return SkillResponse.from(skillService.publish(id));
    }

    /** 审核技能（通过 → PUBLISHED，拒绝 → DRAFT）。 */
    @PostMapping("/{id}/review")
    public SkillResponse review(@PathVariable Long id,
                                @RequestBody Map<String, Object> body) {
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String comment = (String) body.get("comment");
        return SkillResponse.from(skillService.review(id, approved, comment));
    }

    /** 废弃技能（PUBLISHED → DEPRECATED）。 */
    @PostMapping("/{id}/deprecate")
    public SkillResponse deprecate(@PathVariable Long id) {
        return SkillResponse.from(skillService.deprecate(id));
    }

    /** 提交技能反馈（点赞/点踩）。 */
    @PostMapping("/{id}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> feedback(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        String rating = (String) body.get("rating");
        skillService.feedback(id, rating);
        return Map.of("message", "反馈已记录", "rating", rating);
    }
}
