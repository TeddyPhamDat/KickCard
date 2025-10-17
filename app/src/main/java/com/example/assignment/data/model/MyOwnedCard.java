package com.example.assignment.data.model;

public class MyOwnedCard {
    private Long id;
    private String name;
    private String rarity;
    private String team;
    private String description;
    private String baseImageUrl;
    private String status; // APPROVED, SOLD
    private Double price;
    private String ownerUsername;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    // For adapter compatibility
    public String getOwnerName() { return ownerUsername; }
}
