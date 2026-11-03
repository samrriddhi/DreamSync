package com.dreamsync.controller;

import com.dreamsync.entity.Task;
import com.dreamsync.dto.response.TaskResponse;
import com.dreamsync.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Manage project tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create a new task")
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody Task task) {

        TaskResponse savedTask = taskService.createTaskResponse(task);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedTask);
    }

    @Operation(summary = "Get all tasks")
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {

        return ResponseEntity.ok(taskService.getAllTaskResponses());
    }

    @Operation(summary = "Update a task")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task task) {

        TaskResponse updatedTask = taskService.updateTaskResponse(id, task);

        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Delete a task")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.ok("Task deleted successfully");
    }
}