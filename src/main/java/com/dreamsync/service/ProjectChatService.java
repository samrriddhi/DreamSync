package com.dreamsync.service;

import com.dreamsync.dto.request.ChatMessageRequest;
import com.dreamsync.dto.response.ChatMessageResponse;
import com.dreamsync.entity.ChatMessage;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ChatMessageRepository;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProjectChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public ProjectChatService(ChatMessageRepository chatMessageRepository,
                              ProjectRepository projectRepository,
                              ProjectMemberRepository projectMemberRepository,
                              CurrentUserService currentUserService,
                              SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long projectId) {
        Project project = findProject(projectId);
        requireMembership(project, currentUserService.getCurrentUser());
        List<ChatMessage> messages = new ArrayList<>(
                chatMessageRepository.findTop100ByProjectOrderBySentAtDesc(project));
        Collections.reverse(messages);
        return messages.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long projectId, ChatMessageRequest request) {
        Project project = findProject(projectId);
        User sender = currentUserService.getCurrentUser();
        requireMembership(project, sender);

        String content = request.getMessage() == null ? "" : request.getMessage().trim();
        if (content.isEmpty()) throw new IllegalArgumentException("Message cannot be empty");
        if (content.length() > 2000) throw new IllegalArgumentException("Message cannot exceed 2000 characters");

        ChatMessage message = new ChatMessage();
        message.setMessage(content);
        message.setProject(project);
        message.setSender(sender);
        ChatMessageResponse response = toResponse(chatMessageRepository.save(message));

        Runnable broadcast = () -> messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/chat", response);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { broadcast.run(); }
            });
        } else {
            broadcast.run();
        }
        return response;
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void requireMembership(Project project, User user) {
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getMessage(),
                message.getSender().getName(), message.getSentAt());
    }
}
