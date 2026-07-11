package com.dreamsync.service;

import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;
import com.dreamsync.exception.CircularDependencyException;

@Service
public class TaskDependencyService {
    private final CycleDetectionService cycleDetectionService;

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;

    public TaskDependencyService(TaskDependencyRepository taskDependencyRepository,
                                 TaskRepository taskRepository,
                                 CycleDetectionService cycleDetectionService) {

        this.taskDependencyRepository = taskDependencyRepository;
        this.taskRepository = taskRepository;
        this.cycleDetectionService = cycleDetectionService;
    }

    public TaskDependency createDependency(TaskDependency dependency) {

        Long taskId = dependency.getTask().getId();
        Long dependsOnId = dependency.getDependsOnTask().getId();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        Task dependsOnTask = taskRepository.findById(dependsOnId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Dependency task not found"));
        if (cycleDetectionService.hasCycle(taskId, dependsOnId)) {
            throw new CircularDependencyException("Circular dependency detected");
        }

        dependency.setTask(task);
        dependency.setDependsOnTask(dependsOnTask);

        return taskDependencyRepository.save(dependency);
    }
}