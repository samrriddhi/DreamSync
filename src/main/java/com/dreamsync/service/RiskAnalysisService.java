package com.dreamsync.service;

import com.dreamsync.dto.response.RiskAnalysisResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.enums.Priority;
import com.dreamsync.enums.TaskStatus;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RiskAnalysisService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    public RiskAnalysisService(ProjectRepository projectRepository,
                               TaskRepository taskRepository,
                               TaskDependencyRepository taskDependencyRepository) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
    }

    public RiskAnalysisResponse analyzeProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        int totalTasks = tasks.size();
        int completedTasks = 0;
        int overdueTasks = 0;
        int highPriorityTasks = 0;
        int blockedTasks = 0;

        for (Task task : tasks) {

            if (task.getStatus() == TaskStatus.COMPLETED) {
                completedTasks++;
            }

            if (task.getDueDate() != null &&
                    task.getDueDate().isBefore(LocalDate.now()) &&
                    task.getStatus() != TaskStatus.COMPLETED) {

                overdueTasks++;
            }

            if (task.getPriority() == Priority.HIGH ||
                    task.getPriority() == Priority.CRITICAL) {

                highPriorityTasks++;
            }

            List<TaskDependency> dependencies =
                    taskDependencyRepository.findByTaskId(task.getId());

            for (TaskDependency dependency : dependencies) {

                if (dependency.getDependsOnTask().getStatus()
                        != TaskStatus.COMPLETED) {

                    blockedTasks++;
                    break;
                }
            }
        }

        double completionPercentage = 0;

        if (totalTasks > 0) {
            completionPercentage =
                    (completedTasks * 100.0) / totalTasks;
        }

        String riskLevel;

        if (overdueTasks >= 3 || blockedTasks >= 3 || completionPercentage < 40) {
            riskLevel = "HIGH";
        } else if (overdueTasks >= 1 || blockedTasks >= 1 || completionPercentage < 70) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

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