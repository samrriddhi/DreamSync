package com.dreamsync.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String fileType;

    private String filePath;

    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    public Attachment() {
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public Task getTask() {
        return task;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}