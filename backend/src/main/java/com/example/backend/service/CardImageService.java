package com.example.backend.service;

import com.example.backend.model.Card;
import com.example.backend.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CardImageService {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private CardRepository cardRepository;

    // Upload hình ảnh cho thẻ
    public String uploadCardImage(Long cardId, MultipartFile file) throws Exception {
        Card card = findCardById(cardId);

        // Upload hình ảnh lên Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file);

        // Cập nhật URL vào database
        card.setBaseImageUrl(imageUrl);
        cardRepository.save(card);

        return imageUrl;
    }

    // Cập nhật hình ảnh cho thẻ
    public String updateCardImage(Long cardId, MultipartFile file) throws Exception {
        Card card = findCardById(cardId);

        String newImageUrl;

        // Nếu có ảnh cũ, thay thế
        if (hasExistingImage(card)) {
            try {
                String oldPublicId = cloudinaryService.extractPublicId(card.getBaseImageUrl());
                newImageUrl = cloudinaryService.updateImage(file, oldPublicId);
            } catch (Exception e) {
                // Fallback: upload ảnh mới nếu không thể xử lý ảnh cũ
                newImageUrl = cloudinaryService.uploadImage(file);
            }
        } else {
            // Upload ảnh mới
            newImageUrl = cloudinaryService.uploadImage(file);
        }

        // Cập nhật URL mới
        card.setBaseImageUrl(newImageUrl);
        cardRepository.save(card);

        return newImageUrl;
    }

    // Xóa hình ảnh khỏi thẻ
    public void deleteCardImage(Long cardId) throws Exception {
        Card card = findCardById(cardId);

        if (hasExistingImage(card)) {
            try {
                String publicId = cloudinaryService.extractPublicId(card.getBaseImageUrl());
                cloudinaryService.deleteImage(publicId);
            } catch (Exception e) {
                // Log error nhưng vẫn tiếp tục xóa URL trong database
                System.err.println("Không thể xóa ảnh trên Cloudinary: " + e.getMessage());
            }

            // Xóa URL khỏi database
            card.setBaseImageUrl(null);
            cardRepository.save(card);
        }
    }

    // Lấy thông tin hình ảnh của thẻ
    public Map<String, Object> getCardImageInfo(Long cardId) throws Exception {
        Card card = findCardById(cardId);

        if (!hasExistingImage(card)) {
            return Map.of(
                "hasImage", false,
                "message", "Thẻ chưa có hình ảnh"
            );
        }

        try {
            String publicId = cloudinaryService.extractPublicId(card.getBaseImageUrl());
            Map<String, Object> imageInfo = cloudinaryService.getImageInfo(publicId);

            return Map.of(
                "hasImage", true,
                "imageUrl", card.getBaseImageUrl(),
                "publicId", publicId,
                "imageInfo", imageInfo
            );
        } catch (Exception e) {
            return Map.of(
                "hasImage", true,
                "imageUrl", card.getBaseImageUrl(),
                "error", "Không thể lấy thông tin chi tiết: " + e.getMessage()
            );
        }
    }

    // Kiểm tra thẻ có hình ảnh không
    public boolean hasCardImage(Long cardId) {
        try {
            Card card = findCardById(cardId);
            return hasExistingImage(card);
        } catch (Exception e) {
            return false;
        }
    }

    // Lấy URL hình ảnh của thẻ
    public String getCardImageUrl(Long cardId) throws Exception {
        Card card = findCardById(cardId);
        return card.getBaseImageUrl();
    }

    // Validate thẻ có thuộc về user không (để bảo mật)
    public boolean isCardOwnedByUser(Long cardId, Long userId) {
        try {
            Card card = findCardById(cardId);
            return card.getOwnerId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra thẻ có được approved không
    public boolean isCardApproved(Long cardId) {
        try {
            Card card = findCardById(cardId);
            return "APPROVED".equals(card.getStatus());
        } catch (Exception e) {
            return false;
        }
    }

    // Helper methods
    private Card findCardById(Long cardId) throws Exception {
        return cardRepository.findById(cardId)
            .orElseThrow(() -> new Exception("Không tìm thấy thẻ với ID: " + cardId));
    }

    private boolean hasExistingImage(Card card) {
        return card.getBaseImageUrl() != null && !card.getBaseImageUrl().trim().isEmpty();
    }
}
