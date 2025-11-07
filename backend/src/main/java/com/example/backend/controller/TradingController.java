package com.example.backend.controller;

import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
@CrossOrigin(origins = "*")
public class TradingController {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Mua thẻ bằng ví
     */
    @PostMapping("/buy/{cardId}")
    public ResponseEntity<?> buyCard(@PathVariable Long cardId) {
        try {
            User buyer = getCurrentUser();
            if (buyer == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Chưa đăng nhập"
                ));
            }

            Transaction transaction = tradingService.buyCard(cardId, buyer.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "transaction", transaction,
                "message", "Mua thẻ thành công"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách tất cả giao dịch của user
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getMyTransactions() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Chưa đăng nhập"
                ));
            }

            List<Transaction> transactions = tradingService.getUserTransactions(user.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "transactions", transactions,
                "count", transactions.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Lỗi lấy danh sách giao dịch: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách thẻ đã mua
     */
    @GetMapping("/purchases")
    public ResponseEntity<?> getMyPurchases() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Chưa đăng nhập"
                ));
            }

            List<Transaction> purchases = tradingService.getUserPurchases(user.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "purchases", purchases,
                "count", purchases.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Lỗi lấy danh sách mua: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách thẻ đã bán
     */
    @GetMapping("/sales")
    public ResponseEntity<?> getMySales() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Chưa đăng nhập"
                ));
            }

            List<Transaction> sales = tradingService.getUserSales(user.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "sales", sales,
                "count", sales.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Lỗi lấy danh sách bán: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy chi tiết giao dịch
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable Long transactionId) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Chưa đăng nhập"
                ));
            }

            Transaction transaction = tradingService.getTransaction(transactionId);

            // Kiểm tra quyền xem
            if (!user.getId().equals(transaction.getBuyerId()) &&
                !user.getId().equals(transaction.getSellerId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "Không có quyền xem giao dịch này"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "transaction", transaction
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // ===== Helper Methods =====

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}