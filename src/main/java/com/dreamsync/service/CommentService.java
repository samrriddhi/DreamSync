package com.dreamsync.service;

import com.dreamsync.dto.request.CommentRequest;
import com.dreamsync.dto.response.CommentResponse;
import com.dreamsync.entity.Comment;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.CommentRepository;
import com.dreamsync.repository.TaskRepository;
import com.dreamsync.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          UserRepository userRepository,
                          ActivityLogService activityLogService,
                          CurrentUserService currentUserService) {

        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
        this.currentUserService = currentUserService;
    }
    public CommentResponse addComment(Long taskId,
                                      CommentRequest request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        User user = currentUserService.getCurrentUser();

        Comment comment = new Comment();

        comment.setMessage(request.getMessage());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        activityLogService.logActivity(
                "COMMENT_ADDED",
                "Added comment to task: " + task.getTitle()
        );

        CommentResponse response = new CommentResponse();

        response.setId(savedComment.getId());
        response.setMessage(savedComment.getMessage());
        response.setUserName(savedComment.getUser().getName());
        response.setCreatedAt(savedComment.getCreatedAt());

        return response;
    }
    public List<CommentResponse> getComments(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));

        List<Comment> comments =
                commentRepository.findByTaskOrderByCreatedAtAsc(task);

        List<CommentResponse> response = new ArrayList<>();

        for (Comment comment : comments) {

            CommentResponse dto = new CommentResponse();

            dto.setId(comment.getId());
            dto.setMessage(comment.getMessage());
            dto.setUserName(comment.getUser().getName());
            dto.setCreatedAt(comment.getCreatedAt());

            response.add(dto);
        }

        return response;
    }
    public void deleteComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        commentRepository.delete(comment);
    }
    private final ActivityLogService activityLogService;

}