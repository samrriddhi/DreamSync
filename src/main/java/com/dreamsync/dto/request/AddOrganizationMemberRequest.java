package com.dreamsync.dto.request;

import jakarta.validation.constraints.NotNull;

public class AddOrganizationMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
