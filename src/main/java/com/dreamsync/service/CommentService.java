package com.dreamsync.service;

import com.dreamsync.dto.request.CommentRequest;
import com.dreamsync.dto.response.CommentResponse;
import com.dreamsync.entity.Comment;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.CommentRepository;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserService currentUserService;

    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          ProjectMemberRepository projectMemberRepository,
                          ActivityLogService activityLogService,
                          CurrentUserService currentUserService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.activityLogService = activityLogService;
        this.currentUserService = currentUserService;
    }

    public CommentResponse addComment(Long taskId, CommentRequest request) {
        Task task = findTask(taskId);
        User user = currentUserService.getCurrentUser();
        requireMembership(task, user);

        Comment comment = new Comment();
        comment.setMessage(request.getMessage());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        activityLogService.logActivity("COMMENT_ADDED", "Added comment to task: " + task.getTitle());
        return toResponse(savedComment);
    }

    public List<CommentResponse> getComments(Long taskId) {
        Task task = findTask(taskId);
        requireMembership(task, currentUserService.getCurrentUser());

        List<CommentResponse> response = new ArrayList<>();
        for (Comment comment : commentRepository.findByTaskOrderByCreatedAtAsc(task)) {
            response.add(toResponse(comment));
        }
        return response;
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User currentUser = currentUserService.getCurrentUser();
        requireMembership(comment.getTask(), currentUser);

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isProjectManager = projectMemberRepository
                .findByProjectAndUser(comment.getTask().getProject(), currentUser)
                .map(ProjectMember::getRole)
                .filter(role -> role == ProjectRole.PROJECT_MANAGER)
                .isPresent();

        if (!isAuthor && !isProjectManager) {
            throw new AccessDeniedException("Only the comment author or project manager can delete this comment");
        }
        commentRepository.delete(comment);
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void requireMembership(Task task, User user) {
        if (!projectMemberRepository.existsByProjectAndUser(task.getProject(), user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setMessage(comment.getMessage());
        response.setUserName(comment.getUser().getName());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
