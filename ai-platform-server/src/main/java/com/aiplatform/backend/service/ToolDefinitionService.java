package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.ToolDefinitionNotFoundException;
import com.aiplatform.backend.dto.CreateToolDefinitionRequest;
import com.aiplatform.backend.entity.ProjectTool;
import com.aiplatform.backend.entity.ToolDefinition;
import com.aiplatform.backend.mapper.ProjectToolMapper;
import com.aiplatform.backend.mapper.ToolDefinitionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工具定义业务服务。
 *
 * <p>提供工具定义的创建、查询以及项目级工具启用等核心业务逻辑。</p>
 */
@Service
public class ToolDefinitionService {

    private final ToolDefinitionMapper toolDefinitionMapper;
    private final ProjectToolMapper projectToolMapper;

    /**
     * 构造函数，注入所需的数据访问层依赖。
     *
     * @param toolDefinitionMapper 工具定义 Mapper
     * @param projectToolMapper    项目工具 Mapper
     */
    public ToolDefinitionService(ToolDefinitionMapper toolDefinitionMapper, ProjectToolMapper projectToolMapper) {
        this.toolDefinitionMapper = toolDefinitionMapper;
        this.projectToolMapper = projectToolMapper;
    }

    /**
     * 创建工具定义。
     *
     * <p>根据请求参数创建新的工具定义，默认分类为 OTHER，实现方式为 INTERNAL，审计级别为 NORMAL。</p>
     *
     * @param request 创建工具定义请求
     * @return 新创建的工具定义实体
     */
    public ToolDefinition create(CreateToolDefinitionRequest request) {
        ToolDefinition tool = new ToolDefinition();
        tool.setToolName(request.toolName());
        tool.setDisplayName(request.displayName());
        tool.setDescription(request.description());
        tool.setScope(request.scope());
        tool.setProjectId(request.projectId());
        tool.setCategory(request.category() != null ? request.category() : "OTHER");
        tool.setInputSchema(request.inputSchema());
        tool.setOutputSchema(request.outputSchema());
        tool.setImplType(request.implType() != null ? request.implType() : "INTERNAL");
        tool.setImplConfig(request.implConfig());
        tool.setPermissionRequired(request.permissionRequired());
        tool.setAuditLevel(request.auditLevel() != null ? request.auditLevel() : "NORMAL");
        tool.setStatus("ACTIVE");
        toolDefinitionMapper.insert(tool);
        return tool;
    }

    /**
     * 查询所有工具定义列表。
     *
     * @return 按ID升序排列的工具定义列表
     */
    public List<ToolDefinition> list() {
        return toolDefinitionMapper.selectList(Wrappers.<ToolDefinition>lambdaQuery().orderByAsc(ToolDefinition::getId));
    }

    /**
     * 根据ID查询工具定义，不存在则抛出异常。
     *
     * @param id 工具定义ID
     * @return 工具定义实体
     * @throws ToolDefinitionNotFoundException 当工具定义不存在时抛出
     */
    public ToolDefinition getByIdOrThrow(Long id) {
        ToolDefinition tool = toolDefinitionMapper.selectById(id);
        if (tool == null) throw new ToolDefinitionNotFoundException(id);
        return tool;
    }

    // ==================== 项目工具管理 ====================

    /**
     * 为项目启用工具。
     *
     * <p>创建项目与工具定义的关联，启用后项目中的 AI 调用即可使用该工具。</p>
     *
     * @param projectId 项目ID
     * @param toolId    工具定义ID
     * @return 新创建的项目工具关联实体
     * @throws ToolDefinitionNotFoundException 当工具定义不存在时抛出
     */
    public ProjectTool enableForProject(Long projectId, Long toolId) {
        getByIdOrThrow(toolId);
        ProjectTool pt = new ProjectTool();
        pt.setProjectId(projectId);
        pt.setToolId(toolId);
        pt.setStatus("ACTIVE");
        projectToolMapper.insert(pt);
        return pt;
    }

    /**
     * 查询项目已启用的工具列表。
     *
     * @param projectId 项目ID
     * @return 该项目启用的工具关联列表
     */
    public List<ProjectTool> listProjectTools(Long projectId) {
        return projectToolMapper.selectList(Wrappers.<ProjectTool>lambdaQuery()
                .eq(ProjectTool::getProjectId, projectId).orderByAsc(ProjectTool::getId));
    }
}
