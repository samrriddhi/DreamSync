package com.dreamsync.service;

import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;
    private final WebSocketNotificationService webSocketNotificationService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       WebSocketNotificationService webSocketNotificationService,
                       CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.webSocketNotificationService = webSocketNotificationService;
        this.currentUserService = currentUserService;
    }

    public Task createTask(Task task) {
        if (task.getProject() == null || task.getProject().getId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }

        Project project = findProject(task.getProject().getId());
        User currentUser = currentUserService.getCurrentUser();
        requireMembership(project, currentUser);

        task.setId(null);
        task.setProject(project);
        Task savedTask = taskRepository.save(task);

        webSocketNotificationService.sendNotification(
                currentUser.getEmail(), currentUser.getName(), "created task: " + savedTask.getTitle());
        return savedTask;
    }

    public List<Task> getAllTasks() {
        User currentUser = currentUserService.getCurrentUser();
        return projectMemberRepository.findByUser(currentUser).stream()
                .flatMap(member -> taskRepository.findByProjectId(member.getProject().getId()).stream())
                .distinct()
                .toList();
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = findTask(id);
        User currentUser = currentUserService.getCurrentUser();
        requireMembership(existingTask.getProject(), currentUser);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setEstimatedHours(updatedTask.getEstimatedHours());

        Task savedTask = taskRepository.save(existingTask);
        webSocketNotificationService.sendNotification(
                currentUser.getEmail(), currentUser.getName(), "updated task: " + savedTask.getTitle());
        return savedTask;
    }

    public void deleteTask(Long id) {
        Task task = findTask(id);
        User currentUser = currentUserService.getCurrentUser();
        requireMembership(task.getProject(), currentUser);

        String taskTitle = task.getTitle();
        taskRepository.delete(task);
        webSocketNotificationService.sendNotification(
                currentUser.getEmail(), currentUser.getName(), "deleted task: " + taskTitle);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void requireMembership(Project project, User user) {
        if (project == null || !projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }
    }
}
