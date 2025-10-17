package com.example.backend.controller;

import com.example.backend.model.Card;
import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.repository.CardRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TradingController(CardRepository cardRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @PostMapping("/buy/{cardId}")
    @Transactional
    public ResponseEntity<?> buyCard(@PathVariable Long cardId) {
        User buyer = getCurrentUser();
        if (buyer == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // 1. Kiểm tra thẻ tồn tại và có thể mua
        Card card = cardRepository.findById(cardId).orElse(null);
        if (card == null) {
            return ResponseEntity.notFound().build();
        }

        if (!"APPROVED".equals(card.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Card is not available for purchase"));
        }

        if (buyer.getId().equals(card.getOwnerId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot buy your own card"));
        }

        if (card.getPrice() == null || card.getPrice() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Card price not set"));
        }

        // 2. Kiểm tra số dư người mua
        if (buyer.getWalletBalance() < card.getPrice()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Insufficient funds"));
        }

        // 3. Lấy thông tin người bán
        User seller = userRepository.findById(card.getOwnerId()).orElse(null);
        if (seller == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seller not found"));
        }

        try {
            // 4. Thực hiện giao dịch
            double price = card.getPrice();
            double sellerAmount = Math.round(price * 0.95 * 100.0) / 100.0;
            double adminAmount = price - sellerAmount;

            // Trừ tiền người mua
            buyer.setWalletBalance(buyer.getWalletBalance() - price);
            userRepository.save(buyer);

            // Cộng tiền người bán (95%)
            seller.setWalletBalance(seller.getWalletBalance() + sellerAmount);
            userRepository.save(seller);

            // Cộng tiền admin (5%)
            User admin = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().toUpperCase().contains("ADMIN"))
                .findFirst().orElse(null);
            if (admin != null) {
                admin.setWalletBalance(admin.getWalletBalance() + adminAmount);
                userRepository.save(admin);
            }

            // Chuyển quyền sở hữu thẻ
            card.setOwnerId(buyer.getId());
            card.setStatus("SOLD");
            cardRepository.save(card);

            // Tạo transaction record
            Transaction transaction = new Transaction();
            transaction.setCardId(cardId);
            transaction.setBuyerId(buyer.getId());
            transaction.setSellerId(seller.getId());
            transaction.setAmount(price);
            transaction.setStatus("COMPLETED");
            transaction.setCompletedAt(LocalDateTime.now());
            transaction.setNotes("Card purchase: " + card.getName() + ". Seller received: " + sellerAmount + ", Admin received: " + adminAmount);
            transactionRepository.save(transaction);

            return ResponseEntity.ok(Map.of(
                "message", "Purchase successful",
                "transactionId", transaction.getId(),
                "newBalance", buyer.getWalletBalance(),
                "cardId", card.getId(),
                "sellerAmount", sellerAmount,
                "adminAmount", adminAmount
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Transaction failed: " + e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getMyTransactions() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<Transaction> transactions = transactionRepository.findByBuyerIdOrSellerId(user.getId(), user.getId());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/purchases")
    public ResponseEntity<?> getMyPurchases() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<Transaction> purchases = transactionRepository.findByBuyerId(user.getId());
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/transactions/sales")
    public ResponseEntity<?> getMySales() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<Transaction> sales = transactionRepository.findBySellerId(user.getId());
        return ResponseEntity.ok(sales);
    }
}