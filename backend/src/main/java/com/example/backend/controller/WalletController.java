package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final UserRepository userRepository;

    public WalletController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        return ResponseEntity.ok(Map.of(
            "balance", user.getWalletBalance(),
            "username", user.getUsername()
        ));
    }

    @PostMapping("/topup-fake")  // For testing/demo only - direct balance credit
    public ResponseEntity<?> fakeTopUp(@RequestBody Map<String, Double> request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Double amount = request.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
        }

        // Giới hạn nạp tiền tối đa (để demo)
        if (amount > 10000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Maximum top-up amount is $10,000"));
        }

        user.setWalletBalance(user.getWalletBalance() + amount);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", "Top-up successful",
            "amount", amount,
            "newBalance", user.getWalletBalance()
        ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Double> request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Double amount = request.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
        }

        if (user.getWalletBalance() < amount) {
            return ResponseEntity.badRequest().body(Map.of("error", "Insufficient funds"));
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", "Withdrawal successful",
            "amount", amount,
            "newBalance", user.getWalletBalance()
        ));
    }
}