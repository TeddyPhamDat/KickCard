package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Username", nullable = false, unique = true)
    private String username;

    // database column is PasswordHash
    @Column(name = "PasswordHash", nullable = false)
    private String password;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Role")
    private String role; // e.g., "ROLE_USER", "ROLE_ADMIN"

    @Column(name = "Fullname")
    private String fullname;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Address")
    private String address;

    @Column(name = "AvatarUrl")
    private String avatarUrl;

    @Column(name = "WalletBalance")
    private Double walletBalance = 0.0;
}
