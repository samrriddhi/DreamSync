package com.dreamsync.dto.response;

import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Long id;
    private String message;
    private String senderName;
    private LocalDateTime sentAt;

    public ChatMessageResponse() {}
    public ChatMessageResponse(Long id, String message, String senderName, LocalDateTime sentAt) {
        this.id = id;
        this.message = message;
        this.senderName = senderName;
        this.sentAt = sentAt;
    }
    public Long getId() { return id; }
    public String getMessage() { return message; }
    public String getSenderName() { return senderName; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setId(Long id) { this.id = id; }
    public void setMessage(String message) { this.message = message; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
