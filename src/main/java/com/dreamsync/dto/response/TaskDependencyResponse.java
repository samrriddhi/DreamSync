package com.dreamsync.dto.response;

public class TaskDependencyResponse {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long dependsOnTaskId;
    private String dependsOnTaskTitle;

    public TaskDependencyResponse() {
    }

    public TaskDependencyResponse(Long id, Long taskId, String taskTitle,
                                  Long dependsOnTaskId, String dependsOnTaskTitle) {
        this.id = id;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.dependsOnTaskId = dependsOnTaskId;
        this.dependsOnTaskTitle = dependsOnTaskTitle;
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public String getTaskTitle() { return taskTitle; }
    public Long getDependsOnTaskId() { return dependsOnTaskId; }
    public String getDependsOnTaskTitle() { return dependsOnTaskTitle; }
}
