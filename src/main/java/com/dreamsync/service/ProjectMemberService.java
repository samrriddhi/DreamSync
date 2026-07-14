package com.dreamsync.service;

import com.dreamsync.dto.request.AssignMemberRequest;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.dreamsync.dto.response.ProjectMemberResponse;
import java.util.ArrayList;
import java.util.List;
import com.dreamsync.dto.response.UserProjectResponse;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository,
                                ProjectRepository projectRepository,
                                UserRepository userRepository,
                                NotificationService notificationService,
                                ActivityLogService activityLogService) {

        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public String assignMember(Long projectId,
                               AssignMemberRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new RuntimeException("User is already assigned to this project");
        }

        ProjectMember member = new ProjectMember();

        member.setProject(project);
        member.setUser(user);
        member.setRole(request.getRole());

        projectMemberRepository.save(member);
        notificationService.createNotification(
                user,
                "You have been assigned to project: " + project.getName()
        );
        activityLogService.logActivity(
                "MEMBER_ASSIGNED",
                "Assigned " + user.getName() +
                        " to project: " + project.getName()
        );

        return "Member assigned successfully";
    }
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        List<ProjectMember> members =
                projectMemberRepository.findByProject(project);

        List<ProjectMemberResponse> response = new ArrayList<>();

        for (ProjectMember member : members) {

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

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        List<ProjectMember> members =
                projectMemberRepository.findByUser(user);

        List<UserProjectResponse> response = new ArrayList<>();

        for (ProjectMember member : members) {

            UserProjectResponse dto = new UserProjectResponse();

            dto.setProjectId(member.getProject().getId());
            dto.setProjectName(member.getProject().getName());
            dto.setRole(member.getRole().name());

            response.add(dto);
        }

        return response;
    }
}