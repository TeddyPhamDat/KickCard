package com.example.backend.dto;

public class MyCardDTO {
    public Long ownerId;
    public Long id;
    public String name;
    public String rarity;
    public String team;
    public String description;
    public String baseImageUrl;
    public String status; // PENDING, APPROVED, REJECTED, SOLD
    public String rejectionReason;
    public Double price;
}
