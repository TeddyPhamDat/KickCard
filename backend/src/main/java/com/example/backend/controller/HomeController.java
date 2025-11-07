package com.example.backend.controller;

import com.example.backend.dto.HomeListingDTO;
import com.example.backend.model.Card;
import com.example.backend.repository.CardRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final CardRepository cardRepository;
    private final com.example.backend.repository.UserRepository userRepository;

    public HomeController(CardRepository cardRepository, com.example.backend.repository.UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/listings")
    public List<HomeListingDTO> listings(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "team", required = false) String team,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice
    ) {
        // Get all approved cards with price (available for purchase)
        // Lấy user hiện tại để loại bỏ thẻ của chính mình
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                // Nếu UserDetails, lấy username rồi truy vấn userId
                String username = userDetails.getUsername();
                // ...existing code...
                // Để tránh dependency vòng, có thể truyền userId qua request header hoặc JWT claims
            } else if (principal instanceof String username) {
                // ...existing code...
            }
        }

    List<Card> approvedCards = cardRepository.findByStatus("APPROVED").stream()
        .filter(c -> c.getPrice() != null && c.getPrice() > 0)
        .collect(Collectors.toList());

        return approvedCards.stream()
                .filter(c -> {
                    if (name != null && (c.getName() == null || !c.getName().toLowerCase().contains(name.toLowerCase()))) return false;
                    if (rarity != null && (c.getRarity() == null || !c.getRarity().equalsIgnoreCase(rarity))) return false;
                    if (team != null && (c.getTeam() == null || !c.getTeam().equalsIgnoreCase(team))) return false;
                    if (minPrice != null && (c.getPrice() == null || c.getPrice() < minPrice)) return false;
                    if (maxPrice != null && (c.getPrice() == null || c.getPrice() > maxPrice)) return false;
                    return true;
                })
                .map(c -> {
                    HomeListingDTO dto = new HomeListingDTO();
                    dto.cardId = c.getId();
                    dto.cardName = c.getName();
                    dto.rarity = c.getRarity();
                    dto.team = c.getTeam();
                    dto.description = c.getDescription();
                    dto.baseImageUrl = c.getBaseImageUrl();
                    dto.price = c.getPrice();
                    dto.currency = "USD";
                    dto.sellerId = c.getOwnerId();
                    dto.quantity = 1;
                    dto.status = c.getStatus();
                    dto.createdAt = c.getCreatedAt();
                    // Hiển thị tên chủ sở hữu
                    dto.ownerName = "User " + c.getOwnerId(); // Sẽ cập nhật để lấy tên thật nếu cần
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/cards")
    public List<Card> getCardsForSale(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "team", required = false) String team,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice
    ) {
        // Chỉ hiển thị thẻ APPROVED (có thể mua)
        List<Card> approvedCards = cardRepository.findByStatus("APPROVED");

        return approvedCards.stream()
                .filter(card -> {
                    // Filter by name
                    if (name != null && !name.trim().isEmpty()) {
                        if (card.getName() == null || !card.getName().toLowerCase().contains(name.toLowerCase())) {
                            return false;
                        }
                    }

                    // Filter by rarity
                    if (rarity != null && !rarity.trim().isEmpty()) {
                        if (card.getRarity() == null || !card.getRarity().equalsIgnoreCase(rarity)) {
                            return false;
                        }
                    }

                    // Filter by team
                    if (team != null && !team.trim().isEmpty()) {
                        if (card.getTeam() == null || !card.getTeam().equalsIgnoreCase(team)) {
                            return false;
                        }
                    }

                    // Filter by price range
                    if (minPrice != null) {
                        if (card.getPrice() == null || card.getPrice() < minPrice) {
                            return false;
                        }
                    }

                    if (maxPrice != null) {
                        if (card.getPrice() == null || card.getPrice() > maxPrice) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/cards/{id}")
    public org.springframework.http.ResponseEntity<?> cardDetails(@PathVariable Long id) {
        Card card = cardRepository.findById(id).orElse(null);
        if (card == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        
        // Create DTO with owner username
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", card.getId());
        response.put("name", card.getName());
        response.put("rarity", card.getRarity());
        response.put("team", card.getTeam());
        response.put("description", card.getDescription());
        response.put("baseImageUrl", card.getBaseImageUrl());
        response.put("ownerId", card.getOwnerId());
        response.put("price", card.getPrice());
        response.put("status", card.getStatus());
        response.put("rejectionReason", card.getRejectionReason());
        
        // Get owner username
        String ownerUsername = "Unknown";
        if (card.getOwnerId() != null) {
            com.example.backend.model.User owner = userRepository.findById(card.getOwnerId()).orElse(null);
            if (owner != null) {
                ownerUsername = owner.getUsername();
            }
        }
        response.put("ownerUsername", ownerUsername);
        
        return org.springframework.http.ResponseEntity.ok(response);
    }
}


