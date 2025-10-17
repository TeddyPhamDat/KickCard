package com.example.assignment.data.model;

public class User {
    private Long id;
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private String avatarUrl;
    private String role;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullname() { return fullname; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }

    // setters for updating via admin UI
    public void setFullname(String fullname) { this.fullname = fullname; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setRole(String role) { this.role = role; }
}
