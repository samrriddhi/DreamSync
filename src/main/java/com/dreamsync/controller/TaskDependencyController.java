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

    @Operation(summary = "List dependencies for a project")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<java.util.List<com.dreamsync.dto.response.TaskDependencyResponse>> getProjectDependencies(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(taskDependencyService.getProjectDependencies(projectId));
    }

    @Operation(summary = "Delete a task dependency")
    @DeleteMapping("/{dependencyId}")
    public ResponseEntity<Void> deleteDependency(@PathVariable Long dependencyId) {
        taskDependencyService.deleteDependency(dependencyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a task dependency")
    @PostMapping
    public ResponseEntity<TaskDependency> createDependency(
            @RequestBody TaskDependency dependency) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskDependencyService.createDependency(dependency));
    }
}