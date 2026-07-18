package com.dreamsync.service;

import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.stereotype.Service;
import com.dreamsync.entity.User;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;
    private final WebSocketNotificationService webSocketNotificationService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       WebSocketNotificationService webSocketNotificationService,
                       CurrentUserService currentUserService) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.webSocketNotificationService = webSocketNotificationService;
        this.currentUserService = currentUserService;
    }

    public Task createTask(Task task) {

        Long projectId = task.getProject().getId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        task.setProject(project);

        Task savedTask = taskRepository.save(task);

        User currentUser = currentUserService.getCurrentUser();

        webSocketNotificationService.sendNotification(
                currentUser.getName(),
                "created task: " + savedTask.getTitle()
        );

        return savedTask;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task updateTask(Long id, Task updatedTask) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setEstimatedHours(updatedTask.getEstimatedHours());

        Long projectId = updatedTask.getProject().getId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        existingTask.setProject(project);

        Task savedTask = taskRepository.save(existingTask);

        User currentUser = currentUserService.getCurrentUser();

        webSocketNotificationService.sendNotification(
                currentUser.getName(),
                "updated task: " + savedTask.getTitle()
        );

        return savedTask;
    }

    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User currentUser = currentUserService.getCurrentUser();

        String taskTitle = task.getTitle();

        taskRepository.delete(task);

        webSocketNotificationService.sendNotification(
                currentUser.getName(),
                "deleted task: " + taskTitle
        );
    }
}