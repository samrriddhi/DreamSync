package com.dreamsync.service;

import com.dreamsync.dto.response.RiskAnalysisResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.entity.User;
import com.dreamsync.enums.Priority;
import com.dreamsync.enums.TaskStatus;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RiskAnalysisService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public RiskAnalysisService(ProjectRepository projectRepository,
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

    public RiskAnalysisResponse analyzeProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User user = currentUserService.getCurrentUser();
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        int totalTasks = tasks.size();
        int completedTasks = 0, overdueTasks = 0, highPriorityTasks = 0, blockedTasks = 0;

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) completedTasks++;
            if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())
                    && task.getStatus() != TaskStatus.COMPLETED) overdueTasks++;
            if (task.getPriority() == Priority.HIGH || task.getPriority() == Priority.CRITICAL) highPriorityTasks++;

            List<TaskDependency> dependencies = taskDependencyRepository.findByTaskId(task.getId());
            for (TaskDependency dependency : dependencies) {
                if (dependency.getDependsOnTask().getStatus() != TaskStatus.COMPLETED) {
                    blockedTasks++;
                    break;
                }
            }
        }

        double completionPercentage = totalTasks == 0 ? 0 : completedTasks * 100.0 / totalTasks;
        String riskLevel = (overdueTasks >= 3 || blockedTasks >= 3 || completionPercentage < 40)
                ? "HIGH"
                : (overdueTasks >= 1 || blockedTasks >= 1 || completionPercentage < 70)
                ? "MEDIUM" : "LOW";

        RiskAnalysisResponse response = new RiskAnalysisResponse();
        response.setProjectName(project.getName());
        response.setRiskLevel(riskLevel);
        response.setOverdueTasks(overdueTasks);
        response.setBlockedTasks(blockedTasks);
        response.setHighPriorityTasks(highPriorityTasks);
        response.setCompletionPercentage(completionPercentage);
        return response;
    }
}
