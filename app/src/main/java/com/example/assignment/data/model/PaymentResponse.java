package com.example.assignment.data.model;

import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private String orderCode;
    private Double amount;
    private String description;
    private Long buyerId;
    private Long sellerId;
    private Long cardId;
    private String status;
    private String paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

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

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
