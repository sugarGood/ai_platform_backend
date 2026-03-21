package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.SkillNotFoundException;
import com.aiplatform.backend.dto.CreateSkillRequest;
import com.aiplatform.backend.entity.ProjectSkill;
import com.aiplatform.backend.entity.Skill;
import com.aiplatform.backend.mapper.ProjectSkillMapper;
import com.aiplatform.backend.mapper.SkillMapper;
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
     * 查询所有技能列表。
     *
     * @return 按ID升序排列的技能列表
     */
    public List<Skill> list() {
        return skillMapper.selectList(Wrappers.<Skill>lambdaQuery().orderByAsc(Skill::getId));
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
     *
     * @param projectId 项目ID
     * @return 该项目启用的技能关联列表
     */
    public List<ProjectSkill> listProjectSkills(Long projectId) {
        return projectSkillMapper.selectList(Wrappers.<ProjectSkill>lambdaQuery()
                .eq(ProjectSkill::getProjectId, projectId).orderByAsc(ProjectSkill::getId));
    }
}
