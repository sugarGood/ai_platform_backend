package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.SkillNotFoundException;
import com.aiplatform.backend.dto.CreateSkillRequest;
import com.aiplatform.backend.entity.ProjectSkill;
import com.aiplatform.backend.entity.Skill;
import com.aiplatform.backend.mapper.ProjectSkillMapper;
import com.aiplatform.backend.mapper.SkillMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 技能业务服务。
 *
 * <p>提供技能的创建、发布、查询以及项目级技能启用等核心业务逻辑。</p>
 */
@Service
public class SkillService {

    private final SkillMapper skillMapper;
    private final ProjectSkillMapper projectSkillMapper;

    /**
     * 构造函数，注入所需的数据访问层依赖。
     *
     * @param skillMapper        技能 Mapper
     * @param projectSkillMapper 项目技能 Mapper
     */
    public SkillService(SkillMapper skillMapper, ProjectSkillMapper projectSkillMapper) {
        this.skillMapper = skillMapper;
        this.projectSkillMapper = projectSkillMapper;
    }

    /**
     * 创建技能。
     *
     * <p>根据请求参数创建新技能，初始状态为 DRAFT，默认分类为 OTHER，默认版本为 1.0.0。</p>
     *
     * @param request 创建技能请求
     * @return 新创建的技能实体
     */
    public Skill create(CreateSkillRequest request) {
        Skill skill = new Skill();
        skill.setSkillKey(request.skillKey());
        skill.setName(request.name());
        skill.setDescription(request.description());
        skill.setScope(request.scope());
        skill.setProjectId(request.projectId());
        skill.setCategory(request.category() != null ? request.category() : "OTHER");
        skill.setSystemPrompt(request.systemPrompt());
        skill.setKnowledgeRefs(request.knowledgeRefs());
        skill.setBoundTools(request.boundTools());
        skill.setParameters(request.parameters());
        skill.setSlashCommand(request.slashCommand());
        skill.setVersion(request.version() != null ? request.version() : "1.0.0");
        skill.setStatus("DRAFT");
        skill.setUsageCount(0L);
        skill.setSatisfactionUp(0);
        skill.setSatisfactionDown(0);
        skillMapper.insert(skill);
        return skill;
    }

    /**
     * 发布技能。
     *
     * <p>将技能状态从 DRAFT 更新为 PUBLISHED，并记录发布时间。</p>
     *
     * @param id 技能ID
     * @return 更新后的技能实体
     * @throws SkillNotFoundException 当技能不存在时抛出
     */
    public Skill publish(Long id) {
        Skill skill = getByIdOrThrow(id);
        skill.setStatus("PUBLISHED");
        skill.setPublishedAt(LocalDateTime.now());
        skillMapper.updateById(skill);
        return skill;
    }

    /**
     * 查询技能列表，支持按 scope 过滤。
     *
     * @param scope 作用域过滤（GLOBAL/PROJECT），null 表示全部
     * @return 按ID升序排列的技能列表
     */
    public List<Skill> list(String scope) {
        LambdaQueryWrapper<Skill> q = Wrappers.<Skill>lambdaQuery();
        if (scope != null && !scope.isBlank()) q.eq(Skill::getScope, scope);
        q.orderByAsc(Skill::getId);
        return skillMapper.selectList(q);
    }

    /**
     * 根据ID查询技能，不存在则抛出异常。
     *
     * @param id 技能ID
     * @return 技能实体
     * @throws SkillNotFoundException 当技能不存在时抛出
     */
    public Skill getByIdOrThrow(Long id) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) throw new SkillNotFoundException(id);
        return skill;
    }

    // ==================== 项目技能管理 ====================

    /**
     * 为项目启用技能。
     *
     * <p>创建项目与技能的关联，启用后项目成员即可使用该技能。</p>
     *
     * @param projectId 项目ID
     * @param skillId   技能ID
     * @return 新创建的项目技能关联实体
     * @throws SkillNotFoundException 当技能不存在时抛出
     */
    public ProjectSkill enableForProject(Long projectId, Long skillId) {
        getByIdOrThrow(skillId);
        ProjectSkill ps = new ProjectSkill();
        ps.setProjectId(projectId);
        ps.setSkillId(skillId);
        ps.setStatus("ACTIVE");
        projectSkillMapper.insert(ps);
        return ps;
    }

    /**
     * 查询项目已启用的技能列表。
     */
    public List<ProjectSkill> listProjectSkills(Long projectId) {
        return projectSkillMapper.selectList(Wrappers.<ProjectSkill>lambdaQuery()
                .eq(ProjectSkill::getProjectId, projectId).orderByAsc(ProjectSkill::getId));
    }

    /** 编辑技能（仅更新非null字段）。 */
    public Skill update(Long id, CreateSkillRequest request) {
        Skill skill = getByIdOrThrow(id);
        if (request.name() != null) skill.setName(request.name());
        if (request.description() != null) skill.setDescription(request.description());
        if (request.category() != null) skill.setCategory(request.category());
        if (request.systemPrompt() != null) skill.setSystemPrompt(request.systemPrompt());
        if (request.knowledgeRefs() != null) skill.setKnowledgeRefs(request.knowledgeRefs());
        if (request.boundTools() != null) skill.setBoundTools(request.boundTools());
        if (request.parameters() != null) skill.setParameters(request.parameters());
        if (request.slashCommand() != null) skill.setSlashCommand(request.slashCommand());
        skillMapper.updateById(skill);
        return skill;
    }

    /** 审核技能：approved=true → PUBLISHED，否则 → DRAFT。 */
    public Skill review(Long id, boolean approved, String comment) {
        Skill skill = getByIdOrThrow(id);
        skill.setStatus(approved ? "PUBLISHED" : "DRAFT");
        if (approved) skill.setPublishedAt(LocalDateTime.now());
        skillMapper.updateById(skill);
        return skill;
    }

    /** 废弃技能（PUBLISHED → DEPRECATED）。 */
    public Skill deprecate(Long id) {
        Skill skill = getByIdOrThrow(id);
        skill.setStatus("DEPRECATED");
        skillMapper.updateById(skill);
        return skill;
    }

    /** 记录技能反馈（UP/DOWN）。 */
    public void feedback(Long id, String rating) {
        Skill skill = getByIdOrThrow(id);
        if ("UP".equalsIgnoreCase(rating)) {
            skill.setSatisfactionUp(skill.getSatisfactionUp() + 1);
        } else if ("DOWN".equalsIgnoreCase(rating)) {
            skill.setSatisfactionDown(skill.getSatisfactionDown() + 1);
        }
        skillMapper.updateById(skill);
    }

    /** 项目禁用（解绑）技能。 */
    public void disableForProject(Long projectId, Long projectSkillId) {
        projectSkillMapper.delete(Wrappers.<ProjectSkill>lambdaQuery()
                .eq(ProjectSkill::getProjectId, projectId)
                .eq(ProjectSkill::getId, projectSkillId));
    }
}
