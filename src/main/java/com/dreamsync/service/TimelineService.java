package com.dreamsync.service;

import com.dreamsync.dto.response.TimelineResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.User;
import com.dreamsync.exception.ResourceNotFoundException;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TimelineService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public TimelineService(ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           ProjectMemberRepository projectMemberRepository,
                           CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    public TimelineResponse getProjectTimeline(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User user = currentUserService.getCurrentUser();
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new AccessDeniedException("You are not a member of this project's team");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        tasks.sort(Comparator.comparing(Task::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        List<TimelineResponse.TaskTimeline> timeline = new ArrayList<>();
        for (Task task : tasks) {
            TimelineResponse.TaskTimeline dto = new TimelineResponse.TaskTimeline();
            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setDueDate(task.getDueDate());
            dto.setStatus(task.getStatus().name());
            dto.setEstimatedHours(task.getEstimatedHours());
            timeline.add(dto);
        }

        TimelineResponse response = new TimelineResponse();
        response.setProjectName(project.getName());
        response.setTasks(timeline);
        return response;
    }
}
