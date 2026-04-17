package com.group32.cpt202.LY_contributor.dto;

import com.group32.cpt202.LY_contributor.entity.User;

public class UserSummaryResponse {

    private Long id;
    private String username;
    private User.Role role;

    public static UserSummaryResponse fromUser(User user) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }
}
