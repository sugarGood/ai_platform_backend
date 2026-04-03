package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.AssignTaskRequest;
import com.aiplatform.backend.entity.Task;
import com.aiplatform.backend.mapper.TaskMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        var query = Wrappers.<Task>lambdaQuery().eq(Task::getProjectId, projectId);
        if (status != null) {
            query.eq(Task::getStatus, status);
        }
        return taskMapper.selectList(query.orderByDesc(Task::getId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@PathVariable Long projectId, @RequestBody Task body) {
        body.setProjectId(projectId);
        if (body.getStatus() == null) {
            body.setStatus("TODO");
        }
        taskMapper.insert(body);
        return body;
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable Long projectId, @PathVariable Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("Task not found: " + id);
        }
        return task;
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long projectId, @PathVariable Long id,
                       @RequestBody Task body) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("Task not found: " + id);
        }
        if (body.getTitle() != null) {
            task.setTitle(body.getTitle());
        }
        if (body.getStatus() != null) {
            task.setStatus(body.getStatus());
        }
        if (body.getPriority() != null) {
            task.setPriority(body.getPriority());
        }
        if (body.getAssigneeUserId() != null) {
            task.setAssigneeUserId(body.getAssigneeUserId());
        }
        if (body.getDueDate() != null) {
            task.setDueDate(body.getDueDate());
        }
        taskMapper.updateById(task);
        return task;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId, @PathVariable Long id) {
        taskMapper.deleteById(id);
    }

    @PatchMapping("/{id}/assign")
    public Task assign(@PathVariable Long projectId, @PathVariable Long id,
                       @Valid @RequestBody AssignTaskRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("Task not found: " + id);
        }
        task.setAssigneeUserId(request.assigneeUserId());
        taskMapper.updateById(task);
        return task;
    }
}
