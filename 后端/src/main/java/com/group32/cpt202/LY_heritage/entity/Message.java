package com.group32.cpt202.LY_heritage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heritage_id")
    private Long heritageId;

    @Column(name = "user_id")
    private Long userId;

    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public Long getId() {
        return id;
    }

    public Long getHeritageId() {
        return heritageId;
    }

    public void setHeritageId(Long heritageId) {
        this.heritageId = heritageId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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