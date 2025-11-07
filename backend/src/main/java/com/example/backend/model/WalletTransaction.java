package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "UserId", nullable = false)
    private Long userId;

    @Column(name = "OrderCode", nullable = false, unique = true)
    private String orderCode;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "Type", nullable = false)
    private String type; // TOPUP, PURCHASE, REFUND

    @Column(name = "Status", nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, CANCELLED

    @Column(name = "PaymentMethod")
    private String paymentMethod; // VNPAY, WALLET

    @Column(name = "Description")
    private String description;

    @Column(name = "TransactionNo")
    private String transactionNo; // VNPay transaction number

    @Column(name = "BankCode")
    private String bankCode;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
