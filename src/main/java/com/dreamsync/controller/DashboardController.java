package com.dreamsync.controller;

import com.dreamsync.dto.DashboardResponse;
import com.dreamsync.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Project analytics and statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get project dashboard")
    @GetMapping("/project/{projectId}")
    public DashboardResponse getProjectDashboard(
            @PathVariable Long projectId) {

        return dashboardService.getProjectDashboard(projectId);
    }
}