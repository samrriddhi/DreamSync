package com.dreamsync.service;

import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.CommentRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import com.dreamsync.repository.UserRepository;
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
    private CommentRepository commentRepository;
    private TaskDependencyRepository taskDependencyRepository;
    private AttachmentService attachmentService;
    private ActivityLogService activityLogService;
    private NotificationService persistentNotificationService;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private CurrentUserService currentUserService;
    private UserRepository userRepository;
    private WebSocketNotificationService notificationService;
    private TaskService taskService;
    private User currentUser;
    private Project project;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        commentRepository = mock(CommentRepository.class);
        taskDependencyRepository = mock(TaskDependencyRepository.class);
        attachmentService = mock(AttachmentService.class);
        activityLogService = mock(ActivityLogService.class);
        persistentNotificationService = mock(NotificationService.class);
        projectRepository = mock(ProjectRepository.class);
        projectMemberRepository = mock(ProjectMemberRepository.class);
        currentUserService = mock(CurrentUserService.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(WebSocketNotificationService.class);
        taskService = new TaskService(taskRepository, projectRepository,
                projectMemberRepository, notificationService, currentUserService, userRepository,
                commentRepository, taskDependencyRepository, attachmentService, activityLogService, persistentNotificationService);

        currentUser = new User();
        currentUser.setId(10L);
        currentUser.setName("Member");
        currentUser.setEmail("member@example.com");
        project = new Project();
        project.setId(5L);
        project.setName("Project");
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void taskCannotBeAssignedToUserOutsideProject() {
        Task task = new Task();
        task.setTitle("Assigned task");
        task.setProject(project);
        User outsider = new User();
        outsider.setId(22L);
        task.setAssignee(outsider);

        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectAndUser(project, currentUser)).thenReturn(true);
        when(userRepository.findById(22L)).thenReturn(Optional.of(outsider));
        when(projectMemberRepository.existsByProjectAndUser(project, outsider)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> taskService.createTask(task));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void nonMemberCannotCreateTask() {
        Task task = new Task();
        task.setProject(project);
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectAndUser(project, currentUser)).thenReturn(Optional.empty());

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
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(currentUser);
        when(projectMemberRepository.findByProject(project)).thenReturn(List.of(member));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.createTask(task);

        assertNull(created.getId());
        assertSame(project, created.getProject());
        verify(taskRepository).save(task);
        verify(notificationService).sendNotification("member@example.com", "Member", "created task: New task");
    }

    @Test
    void nonMemberCannotUpdateTask() {
        Task existing = new Task();
        existing.setId(7L);
        existing.setProject(project);
        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(projectMemberRepository.findByProjectAndUser(project, currentUser)).thenReturn(Optional.empty());

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
        when(projectMemberRepository.findByProjectAndUser(project, currentUser)).thenReturn(Optional.of(membership(project, currentUser, ProjectRole.PROJECT_MANAGER)));
        existing.setCreatedBy(currentUser);
        when(taskRepository.save(existing)).thenReturn(existing);

        Task result = taskService.updateTask(7L, update);

        assertSame(project, result.getProject());
        assertEquals("After", result.getTitle());
        verify(projectMemberRepository, never()).existsByProjectAndUser(otherProject, currentUser);
    }

    @Test
    void updatePreservesEstimatedHoursWhenClientOmitsThem() {
        Task existing = new Task();
        existing.setId(8L);
        existing.setTitle("Estimate task");
        existing.setEstimatedHours(6);
        existing.setProject(project);
        existing.setCreatedBy(currentUser);

        Task update = new Task();
        update.setTitle("Updated estimate task");

        when(taskRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(projectMemberRepository.findByProjectAndUser(project, currentUser))
                .thenReturn(Optional.of(membership(project, currentUser, ProjectRole.PROJECT_MANAGER)));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task result = taskService.updateTask(8L, update);

        assertEquals(6, result.getEstimatedHours());
        assertEquals("Updated estimate task", result.getTitle());
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
    void deletingTaskAlsoRemovesItsCommentsDependenciesAndAttachments() throws Exception {
        Task existing = new Task();
        existing.setId(7L);
        existing.setTitle("Task with related data");
        existing.setProject(project);
        existing.setCreatedBy(currentUser);

        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(projectMemberRepository.findByProjectAndUser(project, currentUser))
                .thenReturn(Optional.of(membership(project, currentUser, ProjectRole.DEVELOPER)));
        when(commentRepository.findByTaskOrderByCreatedAtAsc(existing)).thenReturn(List.of());
        when(taskDependencyRepository.findByTaskId(7L)).thenReturn(List.of());
        when(taskDependencyRepository.findByDependsOnTaskId(7L)).thenReturn(List.of());

        taskService.deleteTask(7L);

        verify(attachmentService).deleteAttachmentsForTask(existing);
        verify(commentRepository).deleteAll(List.of());
        verify(taskDependencyRepository, times(2)).deleteAll(List.of());
        verify(taskRepository).delete(existing);
    }

    private ProjectMember membership(Project project, User user, ProjectRole role) {
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(role);
        return member;
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
