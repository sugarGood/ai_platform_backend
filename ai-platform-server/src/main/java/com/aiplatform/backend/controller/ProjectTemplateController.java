package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.ProjectTemplate;
import com.aiplatform.backend.mapper.ProjectTemplateMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 代码模板库控制器（模块11）。 */
@RestController
@RequestMapping("/api/templates")
public class ProjectTemplateController {

    private final ProjectTemplateMapper mapper;

    public ProjectTemplateController(ProjectTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public List<ProjectTemplate> list(
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String framework) {
        var q = Wrappers.<ProjectTemplate>lambdaQuery()
                .eq(ProjectTemplate::getStatus, "ACTIVE");
        if (templateType != null) q.eq(ProjectTemplate::getTemplateType, templateType);
        if (language != null) q.eq(ProjectTemplate::getLanguage, language);
        if (framework != null) q.eq(ProjectTemplate::getFramework, framework);
        return mapper.selectList(q.orderByDesc(ProjectTemplate::getId));
    }

    @GetMapping("/{id}")
    public ProjectTemplate getById(@PathVariable Long id) {
        ProjectTemplate e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("Template not found: " + id);
        return e;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectTemplate create(@RequestBody ProjectTemplate body) {
        if (body.getStatus() == null) body.setStatus("ACTIVE");
        if (body.getDownloadCount() == null) body.setDownloadCount(0);
        mapper.insert(body);
        return body;
    }

    @PutMapping("/{id}")
    public ProjectTemplate update(@PathVariable Long id, @RequestBody ProjectTemplate body) {
        ProjectTemplate e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("Template not found: " + id);
        if (body.getName() != null) e.setName(body.getName());
        if (body.getDescription() != null) e.setDescription(body.getDescription());
        if (body.getTemplateContent() != null) e.setTemplateContent(body.getTemplateContent());
        mapper.updateById(e);
        return e;
    }

    @PostMapping("/{id}/archive")
    public ProjectTemplate archive(@PathVariable Long id) {
        ProjectTemplate e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("Template not found: " + id);
        e.setStatus("ARCHIVED");
        mapper.updateById(e);
        return e;
    }
}
