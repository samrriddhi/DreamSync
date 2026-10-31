package com.dreamsync.service;

import com.dreamsync.dto.request.CommentRequest;
import com.dreamsync.entity.*;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {
    private CommentRepository comments;
    private TaskRepository tasks;
    private ProjectMemberRepository members;
    private ActivityLogService logs;
    private CurrentUserService users;
    private CommentService service;
    private User current;
    private Project project;
    private Task task;

    @BeforeEach void setUp() {
        comments = mock(CommentRepository.class);
        tasks = mock(TaskRepository.class);
        members = mock(ProjectMemberRepository.class);
        logs = mock(ActivityLogService.class);
        users = mock(CurrentUserService.class);
        service = new CommentService(comments, tasks, members, logs, users);
        current = new User(); current.setId(10L); current.setName("Current");
        project = new Project(); project.setId(5L);
        task = new Task(); task.setId(7L); task.setTitle("Task"); task.setProject(project);
        when(users.getCurrentUser()).thenReturn(current);
    }

    @Test void nonMemberCannotAddComment() {
        when(tasks.findById(7L)).thenReturn(Optional.of(task));
        when(members.existsByProjectAndUser(project, current)).thenReturn(false);
        CommentRequest request = new CommentRequest(); request.setMessage("Hello");
        assertThrows(AccessDeniedException.class, () -> service.addComment(7L, request));
        verify(comments, never()).save(any());
    }

    @Test void nonMemberCannotReadComments() {
        when(tasks.findById(7L)).thenReturn(Optional.of(task));
        when(members.existsByProjectAndUser(project, current)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.getComments(7L));
        verify(comments, never()).findByTaskOrderByCreatedAtAsc(any());
    }

    @Test void unrelatedMemberCannotDeleteAnotherUsersComment() {
        User author = new User(); author.setId(20L);
        Comment comment = new Comment(); comment.setTask(task); comment.setUser(author);
        ProjectMember membership = new ProjectMember(); membership.setRole(ProjectRole.DEVELOPER);
        when(comments.findById(3L)).thenReturn(Optional.of(comment));
        when(members.existsByProjectAndUser(project, current)).thenReturn(true);
        when(members.findByProjectAndUser(project, current)).thenReturn(Optional.of(membership));
        assertThrows(AccessDeniedException.class, () -> service.deleteComment(3L));
        verify(comments, never()).delete(any(Comment.class));
    }

    @Test void authorCanDeleteOwnComment() {
        Comment comment = new Comment(); comment.setTask(task); comment.setUser(current);
        when(comments.findById(3L)).thenReturn(Optional.of(comment));
        when(members.existsByProjectAndUser(project, current)).thenReturn(true);
        assertDoesNotThrow(() -> service.deleteComment(3L));
        verify(comments).delete(comment);
    }

    @Test void projectManagerCanDeleteAnotherUsersComment() {
        User author = new User(); author.setId(20L);
        Comment comment = new Comment(); comment.setTask(task); comment.setUser(author);
        ProjectMember membership = new ProjectMember(); membership.setRole(ProjectRole.PROJECT_MANAGER);
        when(comments.findById(3L)).thenReturn(Optional.of(comment));
        when(members.existsByProjectAndUser(project, current)).thenReturn(true);
        when(members.findByProjectAndUser(project, current)).thenReturn(Optional.of(membership));
        assertDoesNotThrow(() -> service.deleteComment(3L));
        verify(comments).delete(comment);
    }
}