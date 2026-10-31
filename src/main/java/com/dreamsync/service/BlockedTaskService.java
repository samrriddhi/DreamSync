package com.dreamsync.service;

import com.dreamsync.dto.response.BlockedTaskResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.entity.User;
import com.dreamsync.enums.TaskStatus;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlockedTaskService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public BlockedTaskService(ProjectRepository projectRepository,
                              TaskRepository taskRepository,
                              TaskDependencyRepository taskDependencyRepository,
                              ProjectMemberRepository projectMemberRepository,
                              CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    public List<BlockedTaskResponse> getBlockedTasks(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User user = currentUserService.getCurrentUser();
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<BlockedTaskResponse> blockedTasks = new ArrayList<>();
        for (Task task : tasks) {
            List<TaskDependency> dependencies = taskDependencyRepository.findByTaskId(task.getId());
            for (TaskDependency dependency : dependencies) {
                Task dependsOn = dependency.getDependsOnTask();
                if (dependsOn.getStatus() != TaskStatus.COMPLETED) {
                    BlockedTaskResponse response = new BlockedTaskResponse();
                    response.setId(task.getId());
                    response.setTitle(task.getTitle());
                    response.setBlockedBy(dependsOn.getTitle());
                    blockedTasks.add(response);
                    break;
                }
            }
        }
        return blockedTasks;
    }
}
