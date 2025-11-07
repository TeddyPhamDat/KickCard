package com.example.assignment.data.model;

public class CreatePaymentRequest {
    private Long cardId;
    private Double amount;
    private String description;

    public CreatePaymentRequest() {}

    public CreatePaymentRequest(Long cardId, Double amount, String description) {
        this.cardId = cardId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and setters
    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
