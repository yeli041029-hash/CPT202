package com.group32.cpt202.LY_heritage.dto;

import java.time.LocalDateTime;
import java.util.List;

public class HeritageCommentDTO {

    private Long id;
    private Long parentMessageId;
    private String username;
    private String replyToUsername;
    private String content;
    private LocalDateTime sentAt;
    private List<HeritageCommentDTO> replies;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(Long parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReplyToUsername() {
        return replyToUsername;
    }

    public void setReplyToUsername(String replyToUsername) {
        this.replyToUsername = replyToUsername;
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

    public List<HeritageCommentDTO> getReplies() {
        return replies;
    }

    public void setReplies(List<HeritageCommentDTO> replies) {
        this.replies = replies;
    }
}
