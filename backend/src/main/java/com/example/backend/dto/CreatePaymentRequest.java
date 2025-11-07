package com.example.backend.dto;

public class CreatePaymentRequest {
    public Long cardId;
    public Double amount;
    public String description;

    // Constructors
    public CreatePaymentRequest() {}

    public CreatePaymentRequest(Long cardId, Double amount, String description) {
        this.cardId = cardId;
        this.amount = amount;
        this.description = description;
    }
}
