package com.dreamsync.service;

import com.dreamsync.entity.TaskDependency;
import com.dreamsync.repository.TaskDependencyRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CycleDetectionService {


    private final TaskDependencyRepository taskDependencyRepository;

    public CycleDetectionService(TaskDependencyRepository taskDependencyRepository) {
        this.taskDependencyRepository = taskDependencyRepository;
    }

    public boolean hasCycle(Long taskId, Long dependsOnTaskId) {

        Set<Long> visited = new HashSet<>();

        return dfs(dependsOnTaskId, taskId, visited);
    }

    private boolean dfs(Long currentTaskId,
                        Long targetTaskId,
                        Set<Long> visited) {

        if (currentTaskId.equals(targetTaskId)) {
            return true;
        }

        if (visited.contains(currentTaskId)) {
            return false;
        }

        visited.add(currentTaskId);


        List<TaskDependency> dependencies =
                taskDependencyRepository.findByTaskId(currentTaskId);

        for (TaskDependency dependency : dependencies) {

            if (dfs(dependency.getDependsOnTask().getId(), targetTaskId, visited)) {
                return true;
            }
        }

        return false;
    }
}