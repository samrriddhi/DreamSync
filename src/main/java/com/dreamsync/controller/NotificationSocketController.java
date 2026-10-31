package com.dreamsync.controller;

import com.dreamsync.dto.NotificationMessage;
import com.dreamsync.entity.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class NotificationSocketController {

    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public NotificationMessage sendNotification(NotificationMessage message, Principal principal) {
        Authentication authentication = (Authentication) principal;
        User authenticatedUser = (User) authentication.getPrincipal();
        message.setSender(authenticatedUser.getEmail());
        return message;
    }
}
