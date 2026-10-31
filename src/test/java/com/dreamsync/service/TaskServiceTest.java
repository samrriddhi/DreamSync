package com.dreamsync.service;

import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private CurrentUserService currentUserService;
    private WebSocketNotificationService notificationService;
    private TaskService taskService;
    private User currentUser;
    private Project project;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        projectRepository = mock(ProjectRepository.class);
        projectMemberRepository = mock(ProjectMemberRepository.class);
        currentUserService = mock(CurrentUserService.class);
        notificationService = mock(WebSocketNotificationService.class);
        taskService = new TaskService(taskRepository, projectRepository,
                projectMemberRepository, notificationService, currentUserService);

        currentUser = new User();
        currentUser.setId(10L);
        currentUser.setName("Member");
        project = new Project();
        project.setId(5L);
        project.setName("Project");
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void nonMemberCannotCreateTask() {
        Task task = new Task();
        task.setProject(project);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndUser(project, currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> taskService.createTask(task));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void memberCanCreateTaskAndClientCannotChooseTaskId() {
        Task task = new Task();
        task.setId(999L);
        task.setTitle("New task");
        task.setProject(project);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndUser(project, currentUser)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.createTask(task);

        assertNull(created.getId());
        assertSame(project, created.getProject());
        verify(taskRepository).save(task);
        verify(notificationService).sendNotification("Member", "created task: New task");
    }

    @Test
    void nonMemberCannotUpdateTask() {
        Task existing = new Task();
        existing.setId(7L);
        existing.setProject(project);
        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(projectMemberRepository.existsByProjectAndUser(project, currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(7L, new Task()));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateCannotMoveTaskToAnotherProject() {
        Task existing = new Task();
        existing.setId(7L);
        existing.setTitle("Before");
        existing.setProject(project);
        Project otherProject = new Project();
        otherProject.setId(99L);
        Task update = new Task();
        update.setTitle("After");
        update.setProject(otherProject);

        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(projectMemberRepository.existsByProjectAndUser(project, currentUser)).thenReturn(true);
        when(taskRepository.save(existing)).thenReturn(existing);

        Task result = taskService.updateTask(7L, update);

        assertSame(project, result.getProject());
        assertEquals("After", result.getTitle());
        verify(projectMemberRepository, never()).existsByProjectAndUser(otherProject, currentUser);
    }

    @Test
    void nonMemberCannotDeleteTask() {
        Task existing = new Task();
        existing.setId(7L);
        existing.setProject(project);
        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(projectMemberRepository.existsByProjectAndUser(project, currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(7L));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void taskListingOnlyQueriesProjectsCurrentUserBelongsTo() {
        ProjectMember membership = new ProjectMember();
        membership.setProject(project);
        when(projectMemberRepository.findByUser(currentUser)).thenReturn(List.of(membership));
        when(taskRepository.findByProjectId(5L)).thenReturn(List.of(new Task()));

        List<Task> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        verify(taskRepository).findByProjectId(5L);
        verify(taskRepository, never()).findAll();
    }
}
