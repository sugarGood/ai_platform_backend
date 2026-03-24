package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.Task;
import com.aiplatform.backend.mapper.TaskMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 敏捷任务管理控制器（模块18）。 */
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskMapper taskMapper;

    public TaskController(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public List<Task> list(@PathVariable Long projectId,
                           @RequestParam(required = false) String status) {
        var q = Wrappers.<Task>lambdaQuery().eq(Task::getProjectId, projectId);
        if (status != null) q.eq(Task::getStatus, status);
        return taskMapper.selectList(q.orderByDesc(Task::getId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@PathVariable Long projectId, @RequestBody Task body) {
        body.setProjectId(projectId);
        if (body.getStatus() == null) body.setStatus("TODO");
        taskMapper.insert(body);
        return body;
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable Long projectId, @PathVariable Long id) {
        Task e = taskMapper.selectById(id);
        if (e == null) throw new RuntimeException("Task not found: " + id);
        return e;
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long projectId, @PathVariable Long id,
                       @RequestBody Task body) {
        Task e = taskMapper.selectById(id);
        if (e == null) throw new RuntimeException("Task not found: " + id);
        if (body.getTitle() != null) e.setTitle(body.getTitle());
        if (body.getStatus() != null) e.setStatus(body.getStatus());
        if (body.getPriority() != null) e.setPriority(body.getPriority());
        if (body.getAssigneeUserId() != null) e.setAssigneeUserId(body.getAssigneeUserId());
        if (body.getDueDate() != null) e.setDueDate(body.getDueDate());
        taskMapper.updateById(e);
        return e;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId, @PathVariable Long id) {
        taskMapper.deleteById(id);
    }

    @PatchMapping("/{id}/assign")
    public Task assign(@PathVariable Long projectId, @PathVariable Long id,
                       @RequestBody Map<String, Long> body) {
        Task e = taskMapper.selectById(id);
        if (e == null) throw new RuntimeException("Task not found: " + id);
        e.setAssigneeUserId(body.get("assigneeUserId"));
        taskMapper.updateById(e);
        return e;
    }
}
