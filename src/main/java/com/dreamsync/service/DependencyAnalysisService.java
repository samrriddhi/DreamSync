package com.dreamsync.service;

import com.dreamsync.dto.ImpactAnalysisResponse;
import com.dreamsync.dto.TaskSummaryDTO;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import org.springframework.security.access.AccessDeniedException;
import com.dreamsync.mapper.TaskMapper;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DependencyAnalysisService {

    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public DependencyAnalysisService(TaskDependencyRepository taskDependencyRepository,
                                     TaskRepository taskRepository,
                                     TaskMapper taskMapper,
                                     ProjectMemberRepository projectMemberRepository,
                                     CurrentUserService currentUserService) {
        this.taskDependencyRepository = taskDependencyRepository;
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    public ImpactAnalysisResponse analyzeImpact(Long taskId) {

        Task delayedTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = currentUserService.getCurrentUser();
        if (!projectMemberRepository.existsByProjectAndUser(delayedTask.getProject(), user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }

        List<Task> affectedTasks = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        dfs(taskId, affectedTasks, visited);

        List<TaskSummaryDTO> taskSummaries =
                taskMapper.toSummaryDTOList(affectedTasks);

        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setDelayedTask(delayedTask.getTitle());
        response.setAffectedTaskCount(taskSummaries.size());
        response.setAffectedTasks(taskSummaries);

        return response;
    }

    private void dfs(Long taskId,
                     List<Task> affectedTasks,
                     Set<Long> visited) {

        if (visited.contains(taskId)) {
            return;
        }

        visited.add(taskId);

        List<TaskDependency> dependencies =
                taskDependencyRepository.findByDependsOnTaskId(taskId);

        for (TaskDependency dependency : dependencies) {

            Task affectedTask = dependency.getTask();
            if (!affectedTask.getProject().getId().equals(
                    taskRepository.findById(taskId).orElseThrow().getProject().getId())) {
                continue;
            }

            affectedTasks.add(affectedTask);

            dfs(affectedTask.getId(), affectedTasks, visited);
        }
    }
}