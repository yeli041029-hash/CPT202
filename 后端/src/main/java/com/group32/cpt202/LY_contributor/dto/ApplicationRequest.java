package com.group32.cpt202.LY_contributor.dto;

public class ApplicationRequest {

    private Long userId;
    private String applicationReason;

    // getter 和 setter（必须写！）

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getApplicationReason() {
        return applicationReason;
    }

    public void setApplicationReason(String applicationReason) {
        this.applicationReason = applicationReason;
    }
}