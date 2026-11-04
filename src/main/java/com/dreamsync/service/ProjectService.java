package com.dreamsync.service;

import com.dreamsync.entity.Organization;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.ProjectMember;
import com.dreamsync.entity.User;
import com.dreamsync.enums.ProjectRole;
import com.dreamsync.enums.ProjectStatus;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.OrganizationRepository;
import com.dreamsync.repository.OrganizationMemberRepository;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import com.dreamsync.repository.CommentRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.ChatMessageRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final AttachmentService attachmentService;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserService currentUserService;

    public ProjectService(ProjectRepository projectRepository,
                          OrganizationRepository organizationRepository,
                          ProjectMemberRepository projectMemberRepository,
                          OrganizationMemberRepository organizationMemberRepository,
                          CurrentUserService currentUserService,
                          TaskRepository taskRepository,
                          CommentRepository commentRepository,
                          TaskDependencyRepository taskDependencyRepository,
                          AttachmentService attachmentService,
                          ChatMessageRepository chatMessageRepository) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.currentUserService = currentUserService;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.taskDependencyRepository = taskDependencyRepository;
        this.attachmentService = attachmentService;
        this.chatMessageRepository = chatMessageRepository;
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

        User creator = currentUserService.getCurrentUser();
        if (!organizationMemberRepository.existsByOrganizationAndUser(organization, creator)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You must be an organization member to create a project");
        }

        project.setId(null);
        project.setOrganization(organization);
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.PLANNING);
        }
        Project savedProject = projectRepository.save(project);

        ProjectMember creatorMembership = new ProjectMember();
        creatorMembership.setProject(savedProject);
        creatorMembership.setUser(creator);
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

    @Transactional
    public void deleteProject(Long id) {
        Project project = findProject(id);
        requireProjectManager(project, currentUserService.getCurrentUser());

        List<com.dreamsync.entity.Task> tasks = taskRepository.findByProjectId(id);
        for (com.dreamsync.entity.Task task : tasks) {
            try {
                attachmentService.deleteAttachmentsForTask(task);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not remove project attachments", exception);
            }
            commentRepository.deleteAll(commentRepository.findByTaskOrderByCreatedAtAsc(task));
            taskDependencyRepository.deleteAll(taskDependencyRepository.findByTaskId(task.getId()));
            taskDependencyRepository.deleteAll(taskDependencyRepository.findByDependsOnTaskId(task.getId()));
        }
        taskRepository.deleteAll(tasks);
        chatMessageRepository.deleteByProject(project);
        projectMemberRepository.deleteAll(projectMemberRepository.findByProject(project));
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
