package com.dreamsync.controller;

import com.dreamsync.dto.response.ActivityLogResponse;
import com.dreamsync.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> getAllActivities() {

        return ResponseEntity.ok(
                activityLogService.getAllActivities()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<ActivityLogResponse>> getMyActivities() {

        return ResponseEntity.ok(
                activityLogService.getMyActivities()
        );
    }
}