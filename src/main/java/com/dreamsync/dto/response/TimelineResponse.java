package com.dreamsync.dto.response;

import java.time.LocalDate;
import java.util.List;

public class TimelineResponse {

    private String projectName;

    private List<TaskTimeline> tasks;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<TaskTimeline> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskTimeline> tasks) {
        this.tasks = tasks;
    }

    public static class TaskTimeline {

        private Long id;
        private String title;
        private LocalDate dueDate;
        private String status;
        private Integer estimatedHours;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getEstimatedHours() {
            return estimatedHours;
        }

        public void setEstimatedHours(Integer estimatedHours) {
            this.estimatedHours = estimatedHours;
        }
    }
}