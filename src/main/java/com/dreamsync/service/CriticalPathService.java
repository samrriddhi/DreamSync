package com.dreamsync.service;

import com.dreamsync.dto.response.CriticalPathResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CriticalPathService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public CriticalPathService(ProjectRepository projectRepository,
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

    public CriticalPathResponse findCriticalPath(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = currentUserService.getCurrentUser();
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        Map<Long, Task> taskMap = new HashMap<>();
        Map<Long, List<Long>> graph = new HashMap<>();
        Map<Long, Integer> inDegree = new HashMap<>();

        for (Task task : tasks) {
            taskMap.put(task.getId(), task);
            graph.put(task.getId(), new ArrayList<>());
            inDegree.put(task.getId(), 0);
        }

        // Only dependencies whose two endpoints belong to this project affect its path.
        for (TaskDependency dependency : taskDependencyRepository.findByProjectId(projectId)) {
            Long from = dependency.getDependsOnTask().getId();
            Long to = dependency.getTask().getId();
            if (!taskMap.containsKey(from) || !taskMap.containsKey(to)) {
                continue;
            }
            graph.get(from).add(to);
            inDegree.put(to, inDegree.get(to) + 1);
        }

        List<Long> topologicalOrder = topologicalSort(graph, inDegree);
        if (topologicalOrder.size() != tasks.size()) {
            throw new IllegalStateException("Cannot calculate the critical path because the project contains a dependency cycle");
        }

        Map<Long, Integer> distance = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        for (Task task : tasks) {
            distance.put(task.getId(), estimatedHours(task));
        }

        for (Long current : topologicalOrder) {
            for (Long neighbor : graph.get(current)) {
                int candidate = distance.get(current) + estimatedHours(taskMap.get(neighbor));
                if (candidate > distance.get(neighbor)) {
                    distance.put(neighbor, candidate);
                    previous.put(neighbor, current);
                }
            }
        }

        Long endTask = null;
        int maxHours = 0;
        for (Map.Entry<Long, Integer> entry : distance.entrySet()) {
            if (endTask == null || entry.getValue() > maxHours) {
                endTask = entry.getKey();
                maxHours = entry.getValue();
            }
        }

        List<String> criticalPath = new ArrayList<>();
        while (endTask != null) {
            criticalPath.add(taskMap.get(endTask).getTitle());
            endTask = previous.get(endTask);
        }
        Collections.reverse(criticalPath);

        CriticalPathResponse response = new CriticalPathResponse();
        response.setProjectName(project.getName());
        response.setCriticalPath(criticalPath);
        response.setTotalEstimatedHours(maxHours);
        return response;
    }

    private int estimatedHours(Task task) {
        return task.getEstimatedHours() == null ? 0 : Math.max(0, task.getEstimatedHours());
    }

    private List<Long> topologicalSort(Map<Long, List<Long>> graph,
                                       Map<Long, Integer> inDegree) {
        Queue<Long> queue = new ArrayDeque<>();
        for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<Long> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            order.add(current);
            for (Long neighbor : graph.get(current)) {
                int remaining = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, remaining);
                if (remaining == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        return order;
    }
}
