package com.example.EVProject.dto;

import java.util.Set;

public class ApiResponse {
    private boolean success;
    private String message;
    private String token;
    private Set<String> roles; // ✅ new field

    public ApiResponse(boolean success, String message, String token, Set<String> roles) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.roles = roles;
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public Set<String> getRoles() { return roles; }
}
