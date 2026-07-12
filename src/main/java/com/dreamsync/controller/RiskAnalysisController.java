package com.dreamsync.controller;

import com.dreamsync.dto.response.RiskAnalysisResponse;
import com.dreamsync.service.RiskAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/risk-analysis")
@Tag(name = "Risk Analysis", description = "Project Risk Analysis APIs")
public class RiskAnalysisController {

    private final RiskAnalysisService riskAnalysisService;

    public RiskAnalysisController(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }

    @Operation(summary = "Analyze project risk")
    @GetMapping("/{projectId}")
    public RiskAnalysisResponse analyzeProject(
            @PathVariable Long projectId) {

        return riskAnalysisService.analyzeProject(projectId);
    }
}