package com.dreamsync.mapper;

import com.dreamsync.dto.response.TaskResponse;
import com.dreamsync.entity.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskResponseMapper {

    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setEstimatedHours(task.getEstimatedHours());
        response.setCreatedAt(task.getCreatedAt());

        if (task.getProject() != null) {
            response.setProject(new TaskResponse.ProjectInfo(
                    task.getProject().getId(), task.getProject().getName()));
        }
        if (task.getCreatedBy() != null) {
            response.setCreatedBy(new TaskResponse.UserInfo(
                    task.getCreatedBy().getName(), task.getCreatedBy().getEmail()));
        }
        return response;
    }

    public List<TaskResponse> toResponseList(List<Task> tasks) {
        return tasks.stream().map(this::toResponse).toList();
    }
}