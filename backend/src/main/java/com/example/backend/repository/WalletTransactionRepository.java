package com.example.backend.repository;

import com.example.backend.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<WalletTransaction> findByOrderCode(String orderCode);
    List<WalletTransaction> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    List<WalletTransaction> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
}
