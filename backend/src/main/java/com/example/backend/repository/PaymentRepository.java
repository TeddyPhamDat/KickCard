package com.example.backend.repository;

import com.example.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderCode(String orderCode);

    List<Payment> findByBuyerId(Long buyerId);

    List<Payment> findBySellerId(Long sellerId);

    List<Payment> findByCardId(Long cardId);

    List<Payment> findByStatus(String status);

    Optional<Payment> findByPayOSTransactionId(String payOSTransactionId);

    List<Payment> findByBuyerIdAndStatus(Long buyerId, String status);

    List<Payment> findBySellerIdAndStatus(Long sellerId, String status);
}
