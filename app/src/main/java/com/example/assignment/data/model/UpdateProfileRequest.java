package com.example.assignment.data.model;

public class UpdateProfileRequest {
    private String fullname;
    private String phone;
    private String address;
    private String avatarUrl;

    public UpdateProfileRequest(String fullname, String phone, String address, String avatarUrl) {
        this.fullname = fullname;
        this.phone = phone;
        this.address = address;
        this.avatarUrl = avatarUrl;
    }

    public String getFullname() { return fullname; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getAvatarUrl() { return avatarUrl; }
}
