package com.dreamsync.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = @Index(name = "idx_chat_project_sent_at", columnList = "project_id, sent_at"))
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    @PrePersist
    public void onCreate() { this.sentAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public Project getProject() { return project; }
    public User getSender() { return sender; }
    public void setId(Long id) { this.id = id; }
    public void setMessage(String message) { this.message = message; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public void setProject(Project project) { this.project = project; }
    public void setSender(User sender) { this.sender = sender; }
}
