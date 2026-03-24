package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.DepartmentNotFoundException;
import com.aiplatform.backend.dto.CreateDepartmentRequest;
import com.aiplatform.backend.entity.Department;
import com.aiplatform.backend.mapper.DepartmentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门业务服务。
 * <p>提供部门的创建、查询等操作。</p>
 */
@Service
public class DepartmentService {

    private final DepartmentMapper departmentMapper;

    /**
     * 构造方法，注入部门数据访问层。
     *
     * @param departmentMapper 部门 Mapper
     */
    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    /**
     * 创建新部门。
     * <p>新部门默认状态为 ACTIVE。</p>
     *
     * @param request 创建部门请求参数
     * @return 新创建的部门实体
     */
    public Department create(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name());
        department.setCode(request.code());
        department.setDescription(request.description());
        department.setStatus("ACTIVE");
        departmentMapper.insert(department);
        return department;
    }

    /**
     * 查询全部部门列表，按ID升序排列。
     *
     * @return 部门列表
     */
    public List<Department> list() {
        return departmentMapper.selectList(Wrappers.<Department>lambdaQuery().orderByAsc(Department::getId));
    }

    /**
     * 根据ID查询部门，若不存在则抛出异常。
     *
     * @param id 部门ID
     * @return 部门实体
     * @throws DepartmentNotFoundException 当部门不存在时抛出
     */
    public Department getByIdOrThrow(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new DepartmentNotFoundException(id);
        }
        return department;
    }

    /** 编辑部门（仅更新非null字段）。 */
    public Department update(Long id, CreateDepartmentRequest request) {
        Department department = getByIdOrThrow(id);
        if (request.name() != null) department.setName(request.name());
        if (request.code() != null) department.setCode(request.code());
        if (request.description() != null) department.setDescription(request.description());
        departmentMapper.updateById(department);
        return department;
    }
}
