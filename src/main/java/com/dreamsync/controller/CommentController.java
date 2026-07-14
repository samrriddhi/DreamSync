package com.dreamsync.controller;

import com.dreamsync.dto.request.CommentRequest;
import com.dreamsync.dto.response.CommentResponse;
import com.dreamsync.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request) {

        CommentResponse response =
                commentService.addComment(taskId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                commentService.getComments(taskId)
        );
    }
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId) {

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}