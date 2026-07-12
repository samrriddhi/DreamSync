package com.dreamsync.controller;

import com.dreamsync.dto.response.TimelineResponse;
import com.dreamsync.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/timeline")
@Tag(name = "Timeline", description = "Project Timeline APIs")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @Operation(summary = "Get project timeline")
    @GetMapping("/{projectId}")
    public TimelineResponse getTimeline(
            @PathVariable Long projectId) {

        return timelineService.getProjectTimeline(projectId);
    }
}