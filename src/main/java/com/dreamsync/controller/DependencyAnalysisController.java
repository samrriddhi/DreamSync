package com.dreamsync.controller;

import com.dreamsync.dto.ImpactAnalysisResponse;
import com.dreamsync.service.DependencyAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
@Tag(name = "Dependency Analysis", description = "Analyze task dependencies")
public class DependencyAnalysisController {

    private final DependencyAnalysisService dependencyAnalysisService;

    public DependencyAnalysisController(
            DependencyAnalysisService dependencyAnalysisService) {
        this.dependencyAnalysisService = dependencyAnalysisService;
    }

    @Operation(summary = "Analyze impact of delaying a task")
    @GetMapping("/impact/{taskId}")
    public ImpactAnalysisResponse analyzeImpact(
            @PathVariable Long taskId) {

        return dependencyAnalysisService.analyzeImpact(taskId);
    }
}