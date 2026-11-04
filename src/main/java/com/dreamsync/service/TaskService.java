package com.dreamsync.service;

import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.dto.response.TaskResponse;
import com.dreamsync.mapper.TaskResponseMapper;
import org.springframework.transaction.annotation.Transactional;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import com.dreamsync.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       WebSocketNotificationService webSocketNotificationService,
                       CurrentUserService currentUserService,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.webSocketNotificationService = webSocketNotificationService;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse createTaskResponse(Task task) {
        return new TaskResponseMapper().toResponse(createTask(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTaskResponses() {
        return new TaskResponseMapper().toResponseList(getAllTasks());
    }

    @Transactional
    public TaskResponse updateTaskResponse(Long id, Task task) {
        return new TaskResponseMapper().toResponse(updateTask(id, task));
    }

    public Task createTask(Task task) {
        if (task.getProject() == null || task.getProject().getId() == null)
            throw new IllegalArgumentException("Project ID is required");
        Project project = findProject(task.getProject().getId());
        User currentUser = currentUserService.getCurrentUser();
        requireMembership(project, currentUser);
        task.setId(null);
        task.setProject(project);
        task.setCreatedBy(currentUser);
        if (task.getAssignee() != null) task.setAssignee(validateAssignee(project, task.getAssignee().getId()));
        Task savedTask = taskRepository.save(task);
        notifyProjectMembers(project, currentUser, "created task: " + savedTask.getTitle());
        return savedTask;
    }

    public List<Task> getAllTasks() {
        User currentUser = currentUserService.getCurrentUser();
        return projectMemberRepository.findByUser(currentUser).stream()
                .flatMap(member -> taskRepository.findByProjectId(member.getProject().getId()).stream())
                .distinct().toList();
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = findTask(id);
        User currentUser = currentUserService.getCurrentUser();
        requireTaskMutationPermission(existingTask, currentUser);
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setEstimatedHours(updatedTask.getEstimatedHours());
        existingTask.setAssignee(updatedTask.getAssignee() == null
                ? null
                : validateAssignee(existingTask.getProject(), updatedTask.getAssignee().getId()));
        Task savedTask = taskRepository.save(existingTask);
        notifyProjectMembers(existingTask.getProject(), currentUser, "updated task: " + savedTask.getTitle());
        return savedTask;
    }

    public void deleteTask(Long id) {
        Task task = findTask(id);
        User currentUser = currentUserService.getCurrentUser();
        requireTaskMutationPermission(task, currentUser);
        String taskTitle = task.getTitle();
        taskRepository.delete(task);
        notifyProjectMembers(task.getProject(), currentUser, "deleted task: " + taskTitle);
    }

    private void requireTaskMutationPermission(Task task, User user) {
        ProjectMember membership = projectMemberRepository.findByProjectAndUser(task.getProject(), user)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project's team"));
        if (membership.getRole() == ProjectRole.PROJECT_MANAGER) return;
        if (task.getCreatedBy() == null || !task.getCreatedBy().getId().equals(user.getId()))
            throw new AccessDeniedException("Only the task creator or project manager can modify this task");
    }

    private void notifyProjectMembers(Project project, User sender, String message) {
        projectMemberRepository.findByProject(project).forEach(member ->
                webSocketNotificationService.sendNotification(member.getUser().getEmail(), sender.getName(), message));
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private User validateAssignee(Project project, Long userId) {
        if (userId == null) throw new IllegalArgumentException("Assignee user ID is required");
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        requireMembership(project, assignee);
        return assignee;
    }

    private void requireMembership(Project project, User user) {
        if (project == null || !projectMemberRepository.existsByProjectAndUser(project, user))
            throw new AccessDeniedException("You are not a member of this project's team");
    }
}