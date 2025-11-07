package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request nạp tiền vào ví")
public class TopupRequest {

    @Schema(description = "Số tiền nạp (VND)", example = "100000", required = true)
    private Double amount;

    public TopupRequest() {
    }

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

