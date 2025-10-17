package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String rarity;
    private String team;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String baseImageUrl;
    // owner reference (User.id)
    private Long ownerId;

    // lifecycle status for cards added by users
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, SOLD

    // optional rejection reason provided by admin
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
    
    // price for selling
    private Double price;
    
    private LocalDateTime createdAt = LocalDateTime.now();
}
