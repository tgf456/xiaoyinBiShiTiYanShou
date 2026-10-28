package com.materialmaker.controller;

import com.materialmaker.dto.SubmitRequest;
import com.materialmaker.model.Task;
import com.materialmaker.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Task submit(@RequestHeader(value = "X-User-Id", required = false) String userId,
                       @RequestBody SubmitRequest req) {
        return taskService.submit(userId, req);
    }

    @GetMapping
    public List<Task> history(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return taskService.history(userId);
    }

    @GetMapping("/{id}")
    public Task get(@RequestHeader(value = "X-User-Id", required = false) String userId,
                    @PathVariable String id) {
        return taskService.getById(userId, id);
    }
}