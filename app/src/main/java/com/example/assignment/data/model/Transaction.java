package com.example.assignment.data.model;

public class Transaction {
    private Long id;
    private Long cardId;
    private Long buyerId;
    private Long sellerId;
    private Double amount;
    private String status;
    private String completedAt;
    private String notes;

    public Long getId() { return id; }
    public Long getCardId() { return cardId; }
    public Long getBuyerId() { return buyerId; }
    public Long getSellerId() { return sellerId; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getCompletedAt() { return completedAt; }
    public String getNotes() { return notes; }
}
