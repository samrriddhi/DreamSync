package com.dreamsync.service;

import com.dreamsync.dto.response.ActivityLogResponse;
import com.dreamsync.entity.ActivityLog;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ActivityLogRepository;
import com.dreamsync.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ActivityLogService(ActivityLogRepository activityLogRepository,
                              UserRepository userRepository,
                              CurrentUserService currentUserService) {

        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }
    public void logActivity(String action,
                            String description) {

        User user = currentUserService.getCurrentUser();

        ActivityLog log = new ActivityLog();

        log.setAction(action);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());
        log.setUser(user);

        activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getAllActivities() {

        List<ActivityLog> logs =
                activityLogRepository.findAllByOrderByCreatedAtDesc();

        List<ActivityLogResponse> response = new ArrayList<>();

        for (ActivityLog log : logs) {

            ActivityLogResponse dto = new ActivityLogResponse();

            dto.setUserName(log.getUser().getName());
            dto.setAction(log.getAction());
            dto.setDescription(log.getDescription());
            dto.setCreatedAt(log.getCreatedAt());

            response.add(dto);
        }

        return response;
    }
    public List<ActivityLogResponse> getMyActivities() {

        User user = currentUserService.getCurrentUser();;

        List<ActivityLog> logs =
                activityLogRepository.findByUserOrderByCreatedAtDesc(user);

        List<ActivityLogResponse> response = new ArrayList<>();

        for (ActivityLog log : logs) {

            ActivityLogResponse dto = new ActivityLogResponse();

            dto.setUserName(log.getUser().getName());
            dto.setAction(log.getAction());
            dto.setDescription(log.getDescription());
            dto.setCreatedAt(log.getCreatedAt());

            response.add(dto);
        }

        return response;
    }
}