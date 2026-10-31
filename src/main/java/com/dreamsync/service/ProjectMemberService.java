package com.dreamsync.service;

import com.dreamsync.dto.request.AssignMemberRequest;
import com.dreamsync.dto.response.ProjectMemberResponse;
import com.dreamsync.dto.response.UserProjectResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;
    private final CurrentUserService currentUserService;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository,
                                ProjectRepository projectRepository,
                                UserRepository userRepository,
                                NotificationService notificationService,
                                ActivityLogService activityLogService,
                                CurrentUserService currentUserService) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
        this.currentUserService = currentUserService;
    }

    public String assignMember(Long projectId, AssignMemberRequest request) {
        Project project = findProject(projectId);
        requireProjectManager(project);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getRole() == null || request.getRole() == ProjectRole.PROJECT_MANAGER) {
            throw new IllegalArgumentException("Only an existing project manager can assign the manager role");
        }

        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalStateException("User is already assigned to this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(request.getRole());
        projectMemberRepository.save(member);

        notificationService.createNotification(user,
                "You have been assigned to project: " + project.getName());
        activityLogService.logActivity("MEMBER_ASSIGNED",
                "Assigned " + user.getName() + " to project: " + project.getName());

        return "Member assigned successfully";
    }

    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        Project project = findProject(projectId);
        requireMembership(project);

        List<ProjectMemberResponse> response = new ArrayList<>();
        for (ProjectMember member : projectMemberRepository.findByProject(project)) {
            ProjectMemberResponse dto = new ProjectMemberResponse();
            dto.setUserId(member.getUser().getId());
            dto.setName(member.getUser().getName());
            dto.setEmail(member.getUser().getEmail());
            dto.setRole(member.getRole().name());
            response.add(dto);
        }
        return response;
    }

    public List<UserProjectResponse> getUserProjects(Long userId) {
        User currentUser = currentUserService.getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own projects");
        }

        List<UserProjectResponse> response = new ArrayList<>();
        for (ProjectMember member : projectMemberRepository.findByUser(currentUser)) {
            UserProjectResponse dto = new UserProjectResponse();
            dto.setProjectId(member.getProject().getId());
            dto.setProjectName(member.getProject().getName());
            dto.setRole(member.getRole().name());
            response.add(dto);
        }
        return response;
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void requireMembership(Project project) {
        User currentUser = currentUserService.getCurrentUser();
        if (!projectMemberRepository.existsByProjectAndUser(project, currentUser)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private void requireProjectManager(Project project) {
        User currentUser = currentUserService.getCurrentUser();
        ProjectMember membership = projectMemberRepository.findByProjectAndUser(project, currentUser)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));

        if (membership.getRole() != ProjectRole.PROJECT_MANAGER) {
            throw new AccessDeniedException("Only project managers can manage project members");
        }
    }
}
