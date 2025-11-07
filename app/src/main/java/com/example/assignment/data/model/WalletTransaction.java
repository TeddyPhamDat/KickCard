package com.example.assignment.data.model;

public class WalletTransaction {
    private Long id;
    private String orderCode;
    private Double amount;
    private String type; // TOPUP, PURCHASE, REFUND
    private String status; // PENDING, COMPLETED, FAILED, CANCELLED
    private String paymentMethod; // VNPAY, WALLET
    private String description;
    private String transactionNo;
    private String bankCode;
    private String createdAt;
    private String completedAt;

    // Constructors
    public WalletTransaction() {}

    public WalletTransaction(Long id, String orderCode, Double amount, String type, String status, 
                            String paymentMethod, String description, String transactionNo, 
                            String bankCode, String createdAt, String completedAt) {
        this.id = id;
        this.orderCode = orderCode;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.transactionNo = transactionNo;
        this.bankCode = bankCode;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
