package com.dreamsync.dto.response;

import java.util.List;

public class CriticalPathResponse {

    private String projectName;

    private List<String> criticalPath;

    private int totalEstimatedHours;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<String> getCriticalPath() {
        return criticalPath;
    }

    public void setCriticalPath(List<String> criticalPath) {
        this.criticalPath = criticalPath;
    }

    public int getTotalEstimatedHours() {
        return totalEstimatedHours;
    }

    public void setTotalEstimatedHours(int totalEstimatedHours) {
        this.totalEstimatedHours = totalEstimatedHours;
    }
}