package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateSkillRequest;
import com.aiplatform.backend.dto.SkillResponse;
import com.aiplatform.backend.entity.ProjectSkill;
import com.aiplatform.backend.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 技能管理控制器。
 *
 * <p>提供技能的创建、查询和发布等 REST API，路径前缀为 {@code /api/skills}。</p>
 */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    /**
     * 构造函数，注入技能业务服务。
     *
     * @param skillService 技能业务服务
     */
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    /**
     * 创建技能。
     *
     * @param request 创建技能请求体
     * @return 新创建的技能响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@Valid @RequestBody CreateSkillRequest request) {
        return SkillResponse.from(skillService.create(request));
    }

    /**
     * 查询所有技能列表。
     *
     * @return 技能响应列表
     */
    @GetMapping
    public List<SkillResponse> list() {
        return skillService.list().stream().map(SkillResponse::from).toList();
    }

    /**
     * 根据ID查询技能详情。
     *
     * @param id 技能ID
     * @return 技能响应
     */
    @GetMapping("/{id}")
    public SkillResponse getById(@PathVariable Long id) {
        return SkillResponse.from(skillService.getByIdOrThrow(id));
    }

    /**
     * 发布技能。
     *
     * <p>将技能状态更新为已发布。</p>
     *
     * @param id 技能ID
     * @return 更新后的技能响应
     */
    @PostMapping("/{id}/publish")
    public SkillResponse publish(@PathVariable Long id) {
        return SkillResponse.from(skillService.publish(id));
    }
}
