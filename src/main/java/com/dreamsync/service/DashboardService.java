package com.dreamsync.service;

import com.dreamsync.dto.DashboardResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.enums.Priority;
import com.dreamsync.enums.TaskStatus;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TaskRepository taskRepository) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public DashboardResponse getProjectDashboard(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        int totalTasks = 0;
        int completedTasks = 0;
        int inProgressTasks = 0;
        int todoTasks = 0;
        int highPriorityTasks = 0;

        for (Task task : tasks) {

           

            totalTasks++;

            if (task.getStatus() == TaskStatus.COMPLETED) {
                completedTasks++;
            }

            if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgressTasks++;
            }

            if (task.getStatus() == TaskStatus.TODO) {
                todoTasks++;
            }

            if (task.getPriority() == Priority.HIGH
                    || task.getPriority() == Priority.CRITICAL) {
                highPriorityTasks++;
            }
        }

        double completionPercentage = 0;

        if (totalTasks > 0) {
            completionPercentage =
                    (completedTasks * 100.0) / totalTasks;
        }

        DashboardResponse response = new DashboardResponse();

        response.setProjectName(project.getName());
        response.setTotalTasks(totalTasks);
        response.setCompletedTasks(completedTasks);
        response.setInProgressTasks(inProgressTasks);
        response.setTodoTasks(todoTasks);
        response.setHighPriorityTasks(highPriorityTasks);
        response.setCompletionPercentage(completionPercentage);

        return response;
    }
}