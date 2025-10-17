package com.example.assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("token")
    private String token;
    // Add token if backend returns one

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getter and setter for accessToken if added
}
