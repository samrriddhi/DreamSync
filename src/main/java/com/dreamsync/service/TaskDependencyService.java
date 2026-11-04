package com.dreamsync.service;

import com.dreamsync.dto.response.TaskDependencyResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
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
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public TaskDependencyService(TaskDependencyRepository taskDependencyRepository,
                                 TaskRepository taskRepository,
                                 CycleDetectionService cycleDetectionService,
                                 ProjectMemberRepository projectMemberRepository,
                                 CurrentUserService currentUserService,
                                 ProjectRepository projectRepository) {
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.cycleDetectionService = cycleDetectionService;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    public java.util.List<TaskDependencyResponse> getProjectDependencies(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User user = currentUserService.getCurrentUser();
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }

        return taskDependencyRepository.findByProjectId(projectId).stream()
                .map(dependency -> new TaskDependencyResponse(
                        dependency.getId(),
                        dependency.getTask().getId(),
                        dependency.getTask().getTitle(),
                        dependency.getDependsOnTask().getId(),
                        dependency.getDependsOnTask().getTitle()))
                .toList();
    }

    public void deleteDependency(Long dependencyId) {
        TaskDependency dependency = taskDependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Task dependency not found"));
        Project project = dependency.getTask().getProject();
        User user = currentUserService.getCurrentUser();
        ProjectMember membership = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project's team"));
        if (membership.getRole() != ProjectRole.PROJECT_MANAGER) {
            throw new AccessDeniedException("Only project managers can manage task dependencies");
        }
        taskDependencyRepository.delete(dependency);
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
        boolean alreadyExists = taskDependencyRepository.findByTaskId(taskId).stream()
                .anyMatch(existing -> existing.getDependsOnTask().getId().equals(dependsOnId));
        if (alreadyExists) {
            throw new IllegalStateException("This task dependency already exists");
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