package com.dreamsync.dto.response;

import com.dreamsync.enums.Priority;
import com.dreamsync.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private Integer estimatedHours;
    private LocalDateTime createdAt;
    private ProjectInfo project;
    private UserInfo createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public ProjectInfo getProject() { return project; }
    public void setProject(ProjectInfo project) { this.project = project; }
    public UserInfo getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserInfo createdBy) { this.createdBy = createdBy; }

    public static class ProjectInfo {
        private Long id;
        private String name;
        public ProjectInfo() {}
        public ProjectInfo(Long id, String name) { this.id = id; this.name = name; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class UserInfo {
        private String name;
        private String email;
        public UserInfo() {}
        public UserInfo(String name, String email) { this.name = name; this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}