package com.dreamsync.service;

import com.dreamsync.dto.request.ChatMessageRequest;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.User;
import com.dreamsync.repository.ChatMessageRepository;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProjectChatServiceTest {
    private ChatMessageRepository chatMessageRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private CurrentUserService currentUserService;
    private SimpMessagingTemplate messagingTemplate;
    private ProjectChatService projectChatService;

    @BeforeEach
    void setUp() {
        chatMessageRepository = mock(ChatMessageRepository.class);
        projectRepository = mock(ProjectRepository.class);
        projectMemberRepository = mock(ProjectMemberRepository.class);
        currentUserService = mock(CurrentUserService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        projectChatService = new ProjectChatService(chatMessageRepository, projectRepository,
                projectMemberRepository, currentUserService, messagingTemplate);
    }

    @Test
    void nonMemberCannotSendProjectChatMessage() {
        Project project = new Project();
        project.setId(7L);
        User user = new User();
        user.setEmail("outsider@example.com");
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(projectMemberRepository.existsByProjectAndUser(project, user)).thenReturn(false);

        ChatMessageRequest request = new ChatMessageRequest();
        request.setMessage("This should not be sent");

        assertThrows(AccessDeniedException.class, () -> projectChatService.sendMessage(7L, request));
        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
