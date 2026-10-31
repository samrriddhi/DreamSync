package com.dreamsync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LogoutRequest {

    @NotBlank(message = "Refresh token cannot be empty")
    @Size(max = 500, message = "Refresh token is too long")
    private String refreshToken;

    public LogoutRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}