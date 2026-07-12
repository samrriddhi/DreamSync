package com.dreamsync.service;

import com.dreamsync.dto.response.BlockedTaskResponse;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.enums.TaskStatus;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlockedTaskService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    public BlockedTaskService(ProjectRepository projectRepository,
                              TaskRepository taskRepository,
                              TaskDependencyRepository taskDependencyRepository) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
    }

    public List<BlockedTaskResponse> getBlockedTasks(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        List<BlockedTaskResponse> blockedTasks = new ArrayList<>();

        for (Task task : tasks) {

            List<TaskDependency> dependencies =
                    taskDependencyRepository.findByTaskId(task.getId());

            for (TaskDependency dependency : dependencies) {

                Task dependsOn = dependency.getDependsOnTask();

                if (dependsOn.getStatus() != TaskStatus.COMPLETED) {

                    BlockedTaskResponse response =
                            new BlockedTaskResponse();

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