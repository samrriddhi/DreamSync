package com.dreamsync.controller;

import com.dreamsync.dto.request.AssignMemberRequest;
import com.dreamsync.service.ProjectMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dreamsync.dto.response.ProjectMemberResponse;
import java.util.List;
import com.dreamsync.dto.response.UserProjectResponse;

@RestController
@RequestMapping("/projects")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<String> assignMember(
            @PathVariable Long projectId,
            @RequestBody AssignMemberRequest request) {

        String response = projectMemberService.assignMember(projectId, request);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId) {

        List<ProjectMemberResponse> members =
                projectMemberService.getProjectMembers(projectId);

        return ResponseEntity.ok(members);
    }
    @PutMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody AssignMemberRequest request) {
        projectMemberService.updateMemberRole(projectId, userId, request.getRole());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectMemberService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/projects")
    public ResponseEntity<List<UserProjectResponse>> getUserProjects(
            @PathVariable Long userId) {

        List<UserProjectResponse> projects =
                projectMemberService.getUserProjects(userId);

        return ResponseEntity.ok(projects);
    }
}