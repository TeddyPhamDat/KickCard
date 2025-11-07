package com.example.assignment.data.model;

public class MyCard {
    private Long id;
    private Long ownerId;
    private String name;
    private String rarity;
    private String team;
    private String description;
    private String baseImageUrl;
    private String status; // PENDING, APPROVED, REJECTED, SOLD
    private String rejectionReason;
    private Double price;

    // Constructors
    public MyCard() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBaseImageUrl() { return baseImageUrl; }
    public void setBaseImageUrl(String baseImageUrl) { this.baseImageUrl = baseImageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
