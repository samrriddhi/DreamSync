package com.dreamsync.dto;

import com.dreamsync.entity.Task;

import java.util.List;

public class ImpactAnalysisResponse {

    private String delayedTask;

    private int affectedTaskCount;

    private List<TaskSummaryDTO> affectedTasks;

    public String getDelayedTask() {
        return delayedTask;
    }

    public void setDelayedTask(String delayedTask) {
        this.delayedTask = delayedTask;
    }

    public int getAffectedTaskCount() {
        return affectedTaskCount;
    }

    public void setAffectedTaskCount(int affectedTaskCount) {
        this.affectedTaskCount = affectedTaskCount;
    }

    public List<TaskSummaryDTO> getAffectedTasks() {
        return affectedTasks;
    }

    public void setAffectedTasks(List<TaskSummaryDTO> affectedTasks) {
        this.affectedTasks = affectedTasks;
    }
}