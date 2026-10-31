package com.dreamsync.service;

import com.dreamsync.dto.NotificationMessage;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class WebSocketNotificationServiceTest {

    @Test
    void sendsNotificationOnlyToTheIntendedUsersPrivateQueue() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        WebSocketNotificationService service = new WebSocketNotificationService(messagingTemplate);

        service.sendNotification("recipient@example.com", "Sam", "A task was updated");

        org.mockito.ArgumentCaptor<Object> notificationCaptor =
                org.mockito.ArgumentCaptor.forClass(Object.class);

        verify(messagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("recipient@example.com"),
                org.mockito.ArgumentMatchers.eq("/queue/notifications"),
                notificationCaptor.capture()
        );

        NotificationMessage notification = (NotificationMessage) notificationCaptor.getValue();
        assertEquals("Sam", notification.getSender());
        assertEquals("A task was updated", notification.getMessage());
        verifyNoMoreInteractions(messagingTemplate);
    }
}
