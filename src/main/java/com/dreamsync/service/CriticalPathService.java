package com.dreamsync.service;

import com.dreamsync.dto.response.CriticalPathResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CriticalPathService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    public CriticalPathService(ProjectRepository projectRepository,
                               TaskRepository taskRepository,
                               TaskDependencyRepository taskDependencyRepository) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
    }

    public CriticalPathResponse findCriticalPath(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        List<TaskDependency> dependencies =
                taskDependencyRepository.findAll();

        Map<Long, List<Long>> graph =
                buildGraph(tasks, dependencies);

        Map<Long, Integer> inDegree =
                calculateInDegree(tasks, dependencies);

        List<Long> topologicalOrder =
                topologicalSort(graph, inDegree);

        Map<Long, Task> taskMap = buildTaskMap(tasks);

        Map<Long, Integer> distance = new HashMap<>();

        for (Task task : tasks) {

            distance.put(task.getId(),
                    task.getEstimatedHours());
        }
        Map<Long, Long> previous = new HashMap<>();

        for (Long current : topologicalOrder) {

            for (Long neighbor : graph.get(current)) {

                int currentDistance = distance.get(current);

                int neighborHours =
                        taskMap.get(neighbor).getEstimatedHours();

                if (currentDistance + neighborHours >
                        distance.get(neighbor)) {

                    distance.put(
                            neighbor,
                            currentDistance + neighborHours);

                    previous.put(neighbor, current);
                }
            }
        }

        System.out.println("Distance: " + distance);
        System.out.println("Previous: " + previous);

        System.out.println("Graph: " + graph);
        System.out.println("Topological Order: " + topologicalOrder);

        Long endTask = null;
        int maxHours = 0;

        for (Long taskId : distance.keySet()) {

            if (distance.get(taskId) > maxHours) {
                maxHours = distance.get(taskId);
                endTask = taskId;
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
    Map<Long, Long> previous = new HashMap<>();


    private Map<Long, List<Long>> buildGraph(
            List<Task> tasks,
            List<TaskDependency> dependencies) {

        Map<Long, List<Long>> graph = new HashMap<>();

        for (Task task : tasks) {
            graph.put(task.getId(), new ArrayList<>());
        }

        for (TaskDependency dependency : dependencies) {

            Long from = dependency.getDependsOnTask().getId();
            Long to = dependency.getTask().getId();

            if (!graph.containsKey(from) || !graph.containsKey(to)) {
                continue;
            }

            graph.get(from).add(to);
        }

        return graph;
    }

    private Map<Long, Integer> calculateInDegree(
            List<Task> tasks,
            List<TaskDependency> dependencies) {

        Map<Long, Integer> inDegree = new HashMap<>();

        for (Task task : tasks) {
            inDegree.put(task.getId(), 0);
        }

        for (TaskDependency dependency : dependencies) {

            Long to = dependency.getTask().getId();

            if (!inDegree.containsKey(to)) {
                continue;
            }

            inDegree.put(to, inDegree.get(to) + 1);
        }

        return inDegree;
    }

    private List<Long> topologicalSort(
            Map<Long, List<Long>> graph,
            Map<Long, Integer> inDegree) {

        Queue<Long> queue = new LinkedList<>();

        for (Long taskId : inDegree.keySet()) {

            if (inDegree.get(taskId) == 0) {
                queue.offer(taskId);
            }
        }

        List<Long> topologicalOrder = new ArrayList<>();

        while (!queue.isEmpty()) {

            Long current = queue.poll();

            topologicalOrder.add(current);

            for (Long neighbor : graph.get(current)) {

                inDegree.put(neighbor, inDegree.get(neighbor) - 1);

                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        return topologicalOrder;
    }
    private Map<Long, Task> buildTaskMap(List<Task> tasks) {

        Map<Long, Task> taskMap = new HashMap<>();

        for (Task task : tasks) {
            taskMap.put(task.getId(), task);
        }

        return taskMap;
    }
}