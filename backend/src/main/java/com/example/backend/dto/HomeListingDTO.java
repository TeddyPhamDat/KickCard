package com.example.backend.dto;

import java.time.LocalDateTime;

// DTO for displaying cards in marketplace (renamed but keeping same structure for compatibility)
public class HomeListingDTO {
    // listingId is now optional since we're not using Listing table
    public Long listingId; 
    public Long cardId;
    public String cardName;
    public String rarity;
    public String team;
    public String description;
    public String baseImageUrl;
    public Double price;
    public String currency;
    public Long sellerId; // ownerId from Card
    public Integer quantity; // Always 1 for cards
    public String status; // Card status
    public LocalDateTime createdAt;
    public String ownerName; // Tên chủ sở hữu
}
