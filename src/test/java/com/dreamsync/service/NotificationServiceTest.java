package com.dreamsync.service;

import com.dreamsync.entity.Notification;
import com.dreamsync.entity.User;
import com.dreamsync.repository.NotificationRepository;
import com.dreamsync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    private NotificationRepository notifications;
    private CurrentUserService users;
    private NotificationService service;
    private User current;

    @BeforeEach void setUp() {
        notifications = mock(NotificationRepository.class);
        users = mock(CurrentUserService.class);
        service = new NotificationService(notifications, mock(UserRepository.class), users);
        current = new User(); current.setId(10L);
        when(users.getCurrentUser()).thenReturn(current);
    }

    @Test void cannotMarkAnotherUsersNotificationAsRead() {
        User owner = new User(); owner.setId(20L);
        Notification notification = new Notification();
        notification.setUser(owner); notification.setRead(false);
        when(notifications.findById(4L)).thenReturn(Optional.of(notification));
        assertThrows(AccessDeniedException.class, () -> service.markAsRead(4L));
        assertFalse(notification.isRead());
        verify(notifications, never()).save(any(Notification.class));
    }

    @Test void canMarkOwnNotificationAsRead() {
        Notification notification = new Notification();
        notification.setUser(current); notification.setRead(false);
        when(notifications.findById(4L)).thenReturn(Optional.of(notification));
        assertDoesNotThrow(() -> service.markAsRead(4L));
        assertTrue(notification.isRead());
        verify(notifications).save(notification);
    }
}