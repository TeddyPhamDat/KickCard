package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "CardId", nullable = false)
    private Long cardId;

    @Column(name = "BuyerId", nullable = false)
    private Long buyerId;

    @Column(name = "SellerId", nullable = false)
    private Long sellerId;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "Status")
    private String status = "PENDING"; // PENDING, COMPLETED, FAILED

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @Column(name = "Notes")
    private String notes;
}