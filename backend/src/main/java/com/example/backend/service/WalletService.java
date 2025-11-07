package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy số dư ví của user
     */
    public Double getBalance(Long userId) throws Exception {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception("User not found"));
        return user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
    }

    /**
     * Cộng tiền vào ví
     */
    @Transactional
    public Double addBalance(Long userId, Double amount) throws Exception {
        if (amount < 0) {
            throw new Exception("Amount must be greater than 0");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception("User not found"));

        Double currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
        Double newBalance = currentBalance + amount;

        user.setWalletBalance(newBalance);
        userRepository.save(user);

        return newBalance;
    }

    /**
     * Trừ tiền từ ví
     */
    @Transactional
    public Double deductBalance(Long userId, Double amount) throws Exception {
        if (amount < 0) {
            throw new Exception("Amount must be greater than 0");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception("User not found"));

        Double currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;

        if (currentBalance < amount) {
            throw new Exception("Insufficient balance");
        }

        Double newBalance = currentBalance - amount;
        user.setWalletBalance(newBalance);
        userRepository.save(user);

        return newBalance;
    }

    /**
     * Kiểm tra số dư có đủ không
     */
    public Boolean hasEnoughBalance(Long userId, Double amount) throws Exception {
        Double balance = getBalance(userId);
        return balance >= amount;
    }

    /**
     * Set số dư (cho admin)
     */
    @Transactional
    public Double setBalance(Long userId, Double amount) throws Exception {
        if (amount < 0) {
            throw new Exception("Amount must be greater than or equal to 0");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception("User not found"));

        user.setWalletBalance(amount);
        userRepository.save(user);

        return amount;
    }
}

