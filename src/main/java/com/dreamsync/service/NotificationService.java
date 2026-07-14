package com.dreamsync.service;

import com.dreamsync.dto.response.NotificationResponse;
import com.dreamsync.entity.Notification;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.NotificationRepository;
import com.dreamsync.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               CurrentUserService currentUserService) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }
    public void createNotification(User user,
                                   String message) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }
    public List<NotificationResponse> getMyNotifications() {

        User user = currentUserService.getCurrentUser();

        List<Notification> notifications =
                notificationRepository.findByUserOrderByCreatedAtDesc(user);

        List<NotificationResponse> response = new ArrayList<>();

        for (Notification notification : notifications) {

            NotificationResponse dto = new NotificationResponse();

            dto.setId(notification.getId());
            dto.setMessage(notification.getMessage());
            dto.setRead(notification.isRead());
            dto.setCreatedAt(notification.getCreatedAt());

            response.add(dto);
        }

        return response;
    }
    public void markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);

        notificationRepository.save(notification);
    }

}