package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "OrderCode", unique = true, nullable = false)
    private String orderCode;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "Description")
    private String description;

    @Column(name = "BuyerId", nullable = false)
    private Long buyerId;

    @Column(name = "SellerId", nullable = false)
    private Long sellerId;

    @Column(name = "CardId", nullable = false)
    private Long cardId;

    @Column(name = "Status", nullable = false)
    private String status; // PENDING, PAID, CANCELLED, FAILED

    @Column(name = "PaymentUrl")
    private String paymentUrl;

    @Column(name = "PayOSTransactionId")
    private String payOSTransactionId;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "PaidAt")
    private LocalDateTime paidAt;

    // Constructors
    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Payment(String orderCode, Double amount, String description, Long buyerId, Long sellerId, Long cardId) {
        this();
        this.orderCode = orderCode;
        this.amount = amount;
        this.description = description;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.cardId = cardId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if ("PAID".equals(status) && this.paidAt == null) {
            this.paidAt = LocalDateTime.now();
        }
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getPayOSTransactionId() {
        return payOSTransactionId;
    }

    public void setPayOSTransactionId(String payOSTransactionId) {
        this.payOSTransactionId = payOSTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
