package com.dreamsync.service;

import com.dreamsync.entity.Task;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.repository.ProjectMemberRepository;
import org.springframework.security.access.AccessDeniedException;
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
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public TaskDependencyService(TaskDependencyRepository taskDependencyRepository,
                                 TaskRepository taskRepository,
                                 CycleDetectionService cycleDetectionService,
                                 ProjectMemberRepository projectMemberRepository,
                                 CurrentUserService currentUserService) {
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskRepository = taskRepository;
        this.cycleDetectionService = cycleDetectionService;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
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
        if (!task.getProject().getId().equals(dependsOnTask.getProject().getId())) {
            throw new IllegalArgumentException("Dependencies must belong to the same project");
        }

        User user = currentUserService.getCurrentUser();
        ProjectMember membership = projectMemberRepository
                .findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project's team"));
        if (membership.getRole() != ProjectRole.PROJECT_MANAGER) {
            throw new AccessDeniedException("Only project managers can manage task dependencies");
        }

        if (cycleDetectionService.hasCycle(taskId, dependsOnId)) {
            throw new CircularDependencyException("Circular dependency detected");
        }

        dependency.setTask(task);
        dependency.setDependsOnTask(dependsOnTask);

        return taskDependencyRepository.save(dependency);
    }
}