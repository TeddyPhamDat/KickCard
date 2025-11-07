package com.example.assignment.data.model;

public class TopupRequest {
    private Double amount;

    public TopupRequest() {}

    public TopupRequest(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}