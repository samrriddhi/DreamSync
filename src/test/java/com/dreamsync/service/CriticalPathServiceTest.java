package com.dreamsync.service;

import com.dreamsync.dto.response.CriticalPathResponse;
import com.dreamsync.entity.Project;
import com.dreamsync.entity.Task;
import com.dreamsync.entity.TaskDependency;
import com.dreamsync.entity.User;
import com.dreamsync.repository.ProjectMemberRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.TaskDependencyRepository;
import com.dreamsync.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriticalPathServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskDependencyRepository taskDependencyRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CriticalPathService criticalPathService;

    private Project project;
    private User user;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(10L);
        project.setName("DreamSync");
        user = new User();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(projectMemberRepository.existsByProjectAndUser(project, user)).thenReturn(true);
    }

    @Test
    void calculatesLongestDependencyPathAndIgnoresIndependentTasks() {
        Task design = task(1L, "Design", 2);
        Task implementation = task(2L, "Implementation", 4);
        Task documentation = task(3L, "Documentation", 3);

        TaskDependency dependency = new TaskDependency();
        dependency.setTask(implementation);
        dependency.setDependsOnTask(design);

        when(taskRepository.findByProjectId(10L))
                .thenReturn(List.of(design, implementation, documentation));
        when(taskDependencyRepository.findAll()).thenReturn(List.of(dependency));

        CriticalPathResponse response = criticalPathService.findCriticalPath(10L);

        assertEquals("DreamSync", response.getProjectName());
        assertEquals(List.of("Design", "Implementation"), response.getCriticalPath());
        assertEquals(6, response.getTotalEstimatedHours());
    }

    @Test
    void treatsMissingEstimatesAsZeroAndStillReturnsAPath() {
        Task unestimated = task(1L, "Unestimated task", null);

        when(taskRepository.findByProjectId(10L)).thenReturn(List.of(unestimated));
        when(taskDependencyRepository.findAll()).thenReturn(List.of());

        CriticalPathResponse response = criticalPathService.findCriticalPath(10L);

        assertEquals(List.of("Unestimated task"), response.getCriticalPath());
        assertEquals(0, response.getTotalEstimatedHours());
    }

    private Task task(Long id, String title, Integer hours) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setEstimatedHours(hours);
        return task;
    }
}
