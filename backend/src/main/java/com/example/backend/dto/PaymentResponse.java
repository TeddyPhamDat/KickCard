package com.example.backend.dto;

import java.time.LocalDateTime;

public class PaymentResponse {
    public Long id;
    public String orderCode;
    public Double amount;
    public String description;
    public Long buyerId;
    public Long sellerId;
    public Long cardId;
    public String status;
    public String paymentUrl;
    public LocalDateTime createdAt;
    public LocalDateTime paidAt;

    // Constructors
    public PaymentResponse() {}

    public PaymentResponse(Long id, String orderCode, Double amount, String description,
                          Long buyerId, Long sellerId, Long cardId, String status,
                          String paymentUrl, LocalDateTime createdAt, LocalDateTime paidAt) {
        this.id = id;
        this.orderCode = orderCode;
        this.amount = amount;
        this.description = description;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.cardId = cardId;
        this.status = status;
        this.paymentUrl = paymentUrl;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }
}
