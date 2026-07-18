package com.dreamsync.service;

import com.dreamsync.dto.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(
            SimpMessagingTemplate messagingTemplate) {

        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(String sender,
                                 String message) {

        NotificationMessage notification =
                new NotificationMessage(sender, message);

        messagingTemplate.convertAndSend(
                "/topic/notifications",
                notification
        );
    }
}