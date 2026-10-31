package com.dreamsync.security;

import com.dreamsync.entity.User;
import com.dreamsync.enums.Role;
import com.dreamsync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebSocketAuthenticationInterceptorTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private WebSocketAuthenticationInterceptor interceptor;
    private MessageChannel channel;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        interceptor = new WebSocketAuthenticationInterceptor(jwtService, userRepository);
        channel = mock(MessageChannel.class);
    }

    @Test
    void connectWithoutBearerTokenIsRejected() {
        Message<?> message = stompMessage(StompCommand.CONNECT, null, null, false);
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(message, channel));
    }

    @Test
    void connectWithValidBearerTokenAuthenticatesUser() {
        User user = new User();
        user.setEmail("developer@example.com");
        user.setRole(Role.DEVELOPER);

        when(jwtService.extractEmail("valid-token")).thenReturn("developer@example.com");
        when(jwtService.isTokenValid("valid-token", "developer@example.com")).thenReturn(true);
        when(userRepository.findByEmail("developer@example.com")).thenReturn(Optional.of(user));

        Message<?> message = stompMessage(StompCommand.CONNECT, "Bearer valid-token", null, false);
        assertNotNull(interceptor.preSend(message, channel));
        verify(userRepository).findByEmail("developer@example.com");
    }

    @Test
    void unauthenticatedSendIsRejected() {
        Message<?> message = stompMessage(StompCommand.SEND, null, "/app/notify", false);
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(message, channel));
    }

    @Test
    void authenticatedClientCannotSendBroadcastMessages() {
        Message<?> message = stompMessage(StompCommand.SEND, null, "/app/notify", true);
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(message, channel));
    }

    @Test
    void authenticatedUserCanSubscribeToPrivateNotificationQueue() {
        Message<?> message = stompMessage(
                StompCommand.SUBSCRIBE, null, "/user/queue/notifications", true);
        assertNotNull(interceptor.preSend(message, channel));
    }

    @Test
    void authenticatedUserCannotSubscribeToSharedNotificationTopic() {
        Message<?> message = stompMessage(
                StompCommand.SUBSCRIBE, null, "/topic/notifications", true);
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(message, channel));
    }

    private Message<?> stompMessage(
            StompCommand command, String authorization, String destination, boolean authenticated) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authenticated) {
            User user = new User();
            user.setEmail("developer@example.com");
            user.setRole(Role.DEVELOPER);
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    user, null, List.of()));
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
