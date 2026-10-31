package com.dreamsync.service;

import com.dreamsync.entity.Organization;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.OrganizationRepository;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public ProjectService(ProjectRepository projectRepository,
                          OrganizationRepository organizationRepository,
                          ProjectMemberRepository projectMemberRepository,
                          CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    public Project getProjectById(Long id) {
        Project project = findProject(id);
        requireMembership(project, currentUserService.getCurrentUser());
        return project;
    }

    @Transactional
    public Project createProject(Project project) {
        if (project.getOrganization() == null || project.getOrganization().getId() == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }

        Organization organization = organizationRepository.findById(project.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        project.setId(null);
        project.setOrganization(organization);
        Project savedProject = projectRepository.save(project);

        ProjectMember creatorMembership = new ProjectMember();
        creatorMembership.setProject(savedProject);
        creatorMembership.setUser(currentUserService.getCurrentUser());
        creatorMembership.setRole(ProjectRole.PROJECT_MANAGER);
        projectMemberRepository.save(creatorMembership);

        return savedProject;
    }

    public List<Project> getAllProjects() {
        User currentUser = currentUserService.getCurrentUser();
        return projectMemberRepository.findByUser(currentUser).stream()
                .map(ProjectMember::getProject)
                .toList();
    }

    public Project updateProject(Long id, Project updatedProject) {
        Project existingProject = findProject(id);
        requireProjectManager(existingProject, currentUserService.getCurrentUser());

        existingProject.setName(updatedProject.getName());
        existingProject.setDescription(updatedProject.getDescription());
        existingProject.setStatus(updatedProject.getStatus());

        return projectRepository.save(existingProject);
    }

    public void deleteProject(Long id) {
        Project project = findProject(id);
        requireProjectManager(project, currentUserService.getCurrentUser());
        projectRepository.delete(project);
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void requireMembership(Project project, User user) {
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private void requireProjectManager(Project project, User user) {
        ProjectMember membership = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));

        if (membership.getRole() != ProjectRole.PROJECT_MANAGER) {
            throw new AccessDeniedException("Only project managers can perform this action");
        }
    }
}
