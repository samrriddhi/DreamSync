package com.dreamsync.controller;

import com.dreamsync.dto.NotificationMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationSocketController {

    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public NotificationMessage sendNotification(NotificationMessage message) {

        return message;
    }
}