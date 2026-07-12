package com.dreamsync.controller;

import com.dreamsync.dto.response.BlockedTaskResponse;
import com.dreamsync.service.BlockedTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blocked-tasks")
@Tag(name = "Blocked Tasks", description = "Blocked task analysis APIs")
public class BlockedTaskController {

    private final BlockedTaskService blockedTaskService;

    public BlockedTaskController(BlockedTaskService blockedTaskService) {
        this.blockedTaskService = blockedTaskService;
    }

    @Operation(summary = "Get blocked tasks for a project")
    @GetMapping("/{projectId}")
    public List<BlockedTaskResponse> getBlockedTasks(
            @PathVariable Long projectId) {

        return blockedTaskService.getBlockedTasks(projectId);
    }
}