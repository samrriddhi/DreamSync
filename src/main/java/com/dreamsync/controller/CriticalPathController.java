package com.dreamsync.controller;

import com.dreamsync.dto.response.CriticalPathResponse;
import com.dreamsync.service.CriticalPathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
@Tag(name = "Critical Path", description = "Critical Path Analysis")
public class CriticalPathController {

    private final CriticalPathService criticalPathService;

    public CriticalPathController(CriticalPathService criticalPathService) {
        this.criticalPathService = criticalPathService;
    }

    @Operation(summary = "Find critical path for a project")
    @GetMapping("/critical-path/{projectId}")
    public CriticalPathResponse findCriticalPath(
            @PathVariable Long projectId) {

        return criticalPathService.findCriticalPath(projectId);
    }
}