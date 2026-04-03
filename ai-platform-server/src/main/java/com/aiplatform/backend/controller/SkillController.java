package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateSkillRequest;
import com.aiplatform.backend.dto.SkillFeedbackRequest;
import com.aiplatform.backend.dto.SkillFeedbackResponse;
import com.aiplatform.backend.dto.SkillResponse;
import com.aiplatform.backend.dto.SkillReviewRequest;
import com.aiplatform.backend.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@Valid @RequestBody CreateSkillRequest request) {
        return SkillResponse.from(skillService.create(request));
    }

    @GetMapping
    public List<SkillResponse> list(@RequestParam(required = false) String scope) {
        return skillService.list(scope).stream().map(SkillResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SkillResponse getById(@PathVariable Long id) {
        return SkillResponse.from(skillService.getByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    public SkillResponse update(@PathVariable Long id,
                                @RequestBody CreateSkillRequest request) {
        return SkillResponse.from(skillService.update(id, request));
    }

    @PostMapping("/{id}/publish")
    public SkillResponse publish(@PathVariable Long id) {
        return SkillResponse.from(skillService.publish(id));
    }

    /**
     * 测试技能。
     */
    @PostMapping("/{id}/test")
    public Map<String, Object> test(@PathVariable Long id,
                                    @RequestBody(required = false) Map<String, Object> input) {
        return skillService.test(id, input);
    }

    /**
     * 克隆全局技能到项目。
     */
    @PostMapping("/{id}/clone-to-project")
    public SkillResponse cloneToProject(@PathVariable Long id,
                                        @RequestParam Long projectId) {
        return SkillResponse.from(skillService.cloneToProject(id, projectId));
    }

    @PostMapping("/{id}/review")
    public SkillResponse review(@PathVariable Long id,
                                @Valid @RequestBody SkillReviewRequest request) {
        return SkillResponse.from(skillService.review(id, request.approved(), request.comment()));
    }

    @PostMapping("/{id}/deprecate")
    public SkillResponse deprecate(@PathVariable Long id) {
        return SkillResponse.from(skillService.deprecate(id));
    }

    @PostMapping("/{id}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillFeedbackResponse feedback(@PathVariable Long id,
                                          @Valid @RequestBody SkillFeedbackRequest request) {
        skillService.feedback(id, request.rating());
        return new SkillFeedbackResponse("Feedback recorded", request.rating());
    }
}
