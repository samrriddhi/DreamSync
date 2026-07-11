package com.dreamsync.service;

import com.dreamsync.dto.ImpactAnalysisResponse;
import com.dreamsync.dto.TaskSummaryDTO;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
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

    public DependencyAnalysisService(TaskDependencyRepository taskDependencyRepository,
                                     TaskRepository taskRepository,
                                     TaskMapper taskMapper) {

        this.taskDependencyRepository = taskDependencyRepository;
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public ImpactAnalysisResponse analyzeImpact(Long taskId) {

        List<Task> affectedTasks = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        dfs(taskId, affectedTasks, visited);

        Task delayedTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

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

            affectedTasks.add(affectedTask);

            dfs(affectedTask.getId(), affectedTasks, visited);
        }
    }
}