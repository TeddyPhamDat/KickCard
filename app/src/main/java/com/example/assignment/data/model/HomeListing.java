package com.example.assignment.data.model;

import com.google.gson.annotations.SerializedName;

public class HomeListing {
    public Long listingId;
    public Long cardId;
    public Double price;
    public String currency;
    public Long sellerId;
    public Integer quantity;
    public String createdAt;
    public String cardName;
    public String rarity;
    public String team;
    public String ownerName; // tên chủ sở hữu

    @SerializedName("baseImageUrl")
    public String image;

    public Long getListingId() { return listingId; }
    public Long getCardId() { return cardId; }
    public Double getPrice() { return price; }
    public String getCurrency() { return currency; }
    public Long getSellerId() { return sellerId; }
    public Integer getQuantity() { return quantity; }
    public String getCreatedAt() { return createdAt; }
    public String getCardName() { return cardName; }
    public String getRarity() { return rarity; }
    public String getTeam() { return team; }
    public String getOwnerName() { return ownerName; }
    public String getImage() { return image; }
}
