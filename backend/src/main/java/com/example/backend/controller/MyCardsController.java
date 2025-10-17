package com.example.backend.controller;

import com.example.backend.dto.MyCardDTO;
import com.example.backend.model.Card;
import com.example.backend.model.User;
import com.example.backend.repository.CardRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/my-cards")
public class MyCardsController {
    @GetMapping("/owned")
    public ResponseEntity<?> getOwnedCards() {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

    List<Card> cards = cardRepository.findAll().stream()
        .filter(c -> u.getId().equals(c.getOwnerId()) && ("SOLD".equalsIgnoreCase(c.getStatus()) || "APPROVED".equalsIgnoreCase(c.getStatus()) || "PENDING".equalsIgnoreCase(c.getStatus()) || "REJECTED".equalsIgnoreCase(c.getStatus())))
        .collect(Collectors.toList());

        List<MyCardDTO> dtos = cards.stream().map(c -> {
            MyCardDTO d = new MyCardDTO();
            d.id = c.getId();
            d.name = c.getName();
            d.rarity = c.getRarity();
            d.team = c.getTeam();
            d.description = c.getDescription();
            d.baseImageUrl = c.getBaseImageUrl();
            d.status = c.getStatus();
            d.rejectionReason = c.getRejectionReason();
            d.price = c.getPrice();
            return d;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public MyCardsController(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MyCardDTO dto) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        Card c = new Card();
        c.setName(dto.name);
        c.setRarity(dto.rarity);
        c.setTeam(dto.team);
        c.setDescription(dto.description);
        c.setBaseImageUrl(dto.baseImageUrl);
        c.setPrice(dto.price);
        c.setOwnerId(u.getId());
        c.setStatus("PENDING");

    Card saved = cardRepository.save(c);
    dto.id = saved.getId();
    dto.status = saved.getStatus();
    dto.price = saved.getPrice();
    dto.rarity = saved.getRarity();
    dto.team = saved.getTeam();
    dto.description = saved.getDescription();
    dto.baseImageUrl = saved.getBaseImageUrl();
    dto.name = saved.getName();
    dto.rejectionReason = saved.getRejectionReason();
    return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MyCardDTO dto) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            if (!u.getId().equals(card.getOwnerId())) return ResponseEntity.status(403).body("Forbidden");
            String status = card.getStatus();
            if (!"PENDING".equalsIgnoreCase(status) && !"APPROVED".equalsIgnoreCase(status) && !"SOLD".equalsIgnoreCase(status)) {
                return ResponseEntity.badRequest().body("Only pending, approved, or sold cards can be edited");
            }

            card.setName(dto.name);
            card.setRarity(dto.rarity);
            card.setTeam(dto.team);
            card.setDescription(dto.description);
            card.setBaseImageUrl(dto.baseImageUrl);
            card.setPrice(dto.price);
            // If card is APPROVED or SOLD, set status to PENDING for admin approval
            if ("APPROVED".equalsIgnoreCase(status) || "SOLD".equalsIgnoreCase(status)) {
                card.setStatus("PENDING");
            }
            Card saved = cardRepository.save(card);
            dto.id = saved.getId();
            dto.status = saved.getStatus();
            dto.rejectionReason = saved.getRejectionReason();
            dto.price = saved.getPrice();
            dto.name = saved.getName();
            dto.rarity = saved.getRarity();
            dto.team = saved.getTeam();
            dto.description = saved.getDescription();
            dto.baseImageUrl = saved.getBaseImageUrl();
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            if (!u.getId().equals(card.getOwnerId())) return ResponseEntity.status(403).body("Forbidden");
            if (!"PENDING".equalsIgnoreCase(card.getStatus())) return ResponseEntity.badRequest().body("Only pending cards can be deleted");
            cardRepository.delete(card);
            return ResponseEntity.ok(Map.of("deleted", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> listMyCards() {
    User u = getCurrentUser();
    if (u == null) return ResponseEntity.status(401).body("Unauthorized");

    // Chỉ lấy các thẻ của user có trạng thái SOLD hoặc APPROVED
    List<Card> cards = cardRepository.findAll().stream()
        .filter(c -> u.getId().equals(c.getOwnerId()) && ("SOLD".equalsIgnoreCase(c.getStatus()) || "APPROVED".equalsIgnoreCase(c.getStatus())))
        .collect(Collectors.toList());

    Map<String, List<MyCardDTO>> grouped = cards.stream().map(c -> {
        MyCardDTO d = new MyCardDTO();
        d.id = c.getId();
        d.name = c.getName();
        d.rarity = c.getRarity();
        d.team = c.getTeam();
        d.description = c.getDescription();
        d.baseImageUrl = c.getBaseImageUrl();
        d.status = c.getStatus();
        d.rejectionReason = c.getRejectionReason();
        d.price = c.getPrice();
        return d;
    }).collect(Collectors.groupingBy(d -> d.status == null ? "UNKNOWN" : d.status.toUpperCase()));

    return ResponseEntity.ok(grouped);
    }
}
