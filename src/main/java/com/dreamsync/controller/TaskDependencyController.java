package com.dreamsync.controller;

import com.dreamsync.entity.TaskDependency;
import com.dreamsync.service.TaskDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dependencies")
@Tag(name = "Task Dependencies", description = "Manage task dependencies")
public class TaskDependencyController {

    private final TaskDependencyService taskDependencyService;

    public TaskDependencyController(TaskDependencyService taskDependencyService) {
        this.taskDependencyService = taskDependencyService;
    }

    @Operation(summary = "Create a task dependency")
    @PostMapping
    public ResponseEntity<TaskDependency> createDependency(
            @RequestBody TaskDependency dependency) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskDependencyService.createDependency(dependency));
    }
}