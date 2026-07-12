package com.dreamsync.dto.response;

public class RiskAnalysisResponse {

    private String projectName;
    private String riskLevel;
    private int overdueTasks;
    private int blockedTasks;
    private int highPriorityTasks;
    private double completionPercentage;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(int overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    public int getBlockedTasks() {
        return blockedTasks;
    }

    public void setBlockedTasks(int blockedTasks) {
        this.blockedTasks = blockedTasks;
    }

    public int getHighPriorityTasks() {
        return highPriorityTasks;
    }

    public void setHighPriorityTasks(int highPriorityTasks) {
        this.highPriorityTasks = highPriorityTasks;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}