package com.example.backend.service;

import com.example.backend.model.Card;
import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.repository.CardRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradingService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletService walletService;

    /**
     * Mua thẻ bằng ví (trừ tiền ví và chuyển cho seller)
     */
    @Transactional
    public Transaction buyCard(Long cardId, Long buyerId) throws Exception {
        // Kiểm tra thẻ tồn tại
        Card card = cardRepository.findById(cardId)
            .orElseThrow(() -> new Exception("Không tìm thấy thẻ"));

        // Kiểm tra thẻ có thể mua không
        if (!"APPROVED".equals(card.getStatus())) {
            throw new Exception("Thẻ không khả dụng");
        }

        // Không cho phép mua thẻ của chính mình
        if (card.getOwnerId().equals(buyerId)) {
            throw new Exception("Không thể mua thẻ của chính mình");
        }

        // Kiểm tra giá
        if (card.getPrice() == null || card.getPrice() <= 0) {
            throw new Exception("Giá thẻ không hợp lệ");
        }

        // Kiểm tra số dư ví
        if (!walletService.hasEnoughBalance(buyerId, card.getPrice())) {
            throw new Exception("Số dư ví không đủ");
        }

        // Lấy thông tin người bán
        User seller = userRepository.findById(card.getOwnerId())
            .orElseThrow(() -> new Exception("Không tìm thấy người bán"));

        // Tính tiền
        Double price = card.getPrice();
        Double sellerAmount = Math.round(price * 0.95 * 100.0) / 100.0; // 95% cho seller
        Double adminAmount = price - sellerAmount; // 5% phí

        // Trừ tiền người mua
        walletService.deductBalance(buyerId, price);

        // Cộng tiền người bán
        walletService.addBalance(seller.getId(), sellerAmount);

        // Cộng tiền admin (nếu có)
        try {
            User admin = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().toUpperCase().contains("ROLE_ADMIN"))
                .findFirst()
                .orElse(null);
            if (admin != null) {
                walletService.addBalance(admin.getId(), adminAmount);
            }
        } catch (Exception e) {
            System.err.println("Lỗi cộng tiền admin: " + e.getMessage());
        }

        // Chuyển quyền sở hữu thẻ
        card.setOwnerId(buyerId);
        card.setStatus("SOLD");
        cardRepository.save(card);

        // Tạo transaction record
        Transaction transaction = new Transaction();
        transaction.setCardId(cardId);
        transaction.setBuyerId(buyerId);
        transaction.setSellerId(seller.getId());
        transaction.setAmount(price);
        transaction.setStatus("COMPLETED");
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setNotes("Mua thẻ: " + card.getName() + ". Seller nhận: " + sellerAmount + ", Admin nhận: " + adminAmount);

        return transactionRepository.save(transaction);
    }

    /**
     * Lấy danh sách transaction của user
     */
    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByBuyerIdOrSellerId(userId, userId);
    }

    /**
     * Lấy danh sách thẻ mua của user
     */
    public List<Transaction> getUserPurchases(Long userId) {
        return transactionRepository.findByBuyerId(userId);
    }

    /**
     * Lấy danh sách thẻ bán của user
     */
    public List<Transaction> getUserSales(Long userId) {
        return transactionRepository.findBySellerId(userId);
    }

    /**
     * Lấy chi tiết transaction
     */
    public Transaction getTransaction(Long transactionId) throws Exception {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new Exception("Transaction not found"));
    }
}

