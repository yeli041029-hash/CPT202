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

    @Column(name = "parent_message_id")
    private Long parentMessageId;

    @Column(name = "forum_post_id")
    private Long forumPostId;

    @Column(name = "legacy_heritage_item_id")
    private Long legacyHeritageItemId;

    private String title;

    @Lob
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    private String tags;

    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "share_count")
    private Long shareCount;

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

    public Long getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(Long parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public Long getForumPostId() {
        return forumPostId;
    }

    public void setForumPostId(Long forumPostId) {
        this.forumPostId = forumPostId;
    }

    public Long getLegacyHeritageItemId() {
        return legacyHeritageItemId;
    }

    public void setLegacyHeritageItemId(Long legacyHeritageItemId) {
        this.legacyHeritageItemId = legacyHeritageItemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
