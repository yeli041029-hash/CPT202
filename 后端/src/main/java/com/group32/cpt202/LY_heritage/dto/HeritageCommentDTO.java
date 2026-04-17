package com.group32.cpt202.LY_heritage.dto;

import java.time.LocalDateTime;

public class HeritageCommentDTO {

    private String username;
    private String content;
    private LocalDateTime sentAt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}