package com.group32.cpt202.LY_heritage.dto;

import java.time.LocalDateTime;
import java.util.List;

public class HeritageDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String location;
    private String imageUrl;
    private Long contributorId;
    private String contributorName;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<HeritageCommentDTO> comments;
    private List<SimpleHeritageDTO> recommendations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getContributorId() {
        return contributorId;
    }

    public void setContributorId(Long contributorId) {
        this.contributorId = contributorId;
    }

    public String getContributorName() {
        return contributorName;
    }

    public void setContributorName(String contributorName) {
        this.contributorName = contributorName;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<HeritageCommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<HeritageCommentDTO> comments) {
        this.comments = comments;
    }

    public List<SimpleHeritageDTO> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<SimpleHeritageDTO> recommendations) {
        this.recommendations = recommendations;
    }
}