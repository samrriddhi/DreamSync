package com.dreamsync.controller;

import com.dreamsync.dto.request.ChatMessageRequest;
import com.dreamsync.dto.response.ChatMessageResponse;
import com.dreamsync.service.ProjectChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/chat")
public class ProjectChatController {
    private final ProjectChatService projectChatService;

    public ProjectChatController(ProjectChatService projectChatService) {
        this.projectChatService = projectChatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectChatService.getMessages(projectId));
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long projectId,
            @Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectChatService.sendMessage(projectId, request));
    }
}
