package com.dreamsync.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "task_dependencies")
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "depends_on_task_id")
    private Task dependsOnTask;

    public TaskDependency() {
    }

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public Task getDependsOnTask() {
        return dependsOnTask;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setDependsOnTask(Task dependsOnTask) {
        this.dependsOnTask = dependsOnTask;
    }
}