package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.ProjectSkill;
import com.aiplatform.backend.service.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目技能管理控制器。
 *
 * <p>管理项目级技能的启用、查询和禁用，路径前缀为 {@code /api/projects/{projectId}/skills}。</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/skills")
public class ProjectSkillController {

    private final SkillService skillService;

    public ProjectSkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    /** 为项目启用技能。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectSkill enable(@PathVariable Long projectId, @RequestParam Long skillId) {
        return skillService.enableForProject(projectId, skillId);
    }

    /** 查询项目已启用的技能列表。 */
    @GetMapping
    public List<ProjectSkill> list(@PathVariable Long projectId) {
        return skillService.listProjectSkills(projectId);
    }

    /** 项目禁用（解绑）技能。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable Long projectId, @PathVariable Long id) {
        skillService.disableForProject(projectId, id);
    }
}
