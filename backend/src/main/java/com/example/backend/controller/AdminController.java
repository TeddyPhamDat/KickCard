package com.example.backend.controller;

import com.example.backend.model.Card;
import com.example.backend.model.User;
import com.example.backend.repository.CardRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public AdminController(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }



    // --- Cards management (new) ---
    @GetMapping("/cards/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Card> pendingCards() {
        return cardRepository.findByStatus("PENDING");
    }

    @PostMapping("/cards/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCard(@PathVariable Long id) {
        return cardRepository.findById(id).map(c -> {
            c.setStatus("APPROVED");
            cardRepository.save(c);
            java.util.Map<String, String> resp = java.util.Map.of("message", "Card approved");
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/cards/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectCard(@PathVariable Long id, @RequestParam(value = "reason", required = false) String reason) {
        return cardRepository.findById(id).map(c -> {
            c.setStatus("REJECTED");
            c.setRejectionReason(reason);
            cardRepository.save(c);
            java.util.Map<String, String> resp = java.util.Map.of("message", "Card rejected");
            // TODO: notify owner via notification system (not implemented)
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    // View any card
    @GetMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Card> getCard(@PathVariable Long id) {
        return cardRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update any card (admin override)
    @PutMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCard(@PathVariable Long id, @RequestBody Card update) {
        return cardRepository.findById(id).map(c -> {
            // apply editable fields
            c.setName(update.getName());
            c.setRarity(update.getRarity());
            c.setTeam(update.getTeam());
            c.setDescription(update.getDescription());
            c.setBaseImageUrl(update.getBaseImageUrl());
            c.setPrice(update.getPrice());
            c.setStatus(update.getStatus());
            c.setRejectionReason(update.getRejectionReason());
            cardRepository.save(c);
            return ResponseEntity.ok(c);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete any card
    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        return cardRepository.findById(id).map(c -> {
            cardRepository.delete(c);
            return ResponseEntity.ok("Deleted");
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Users management (new) ---
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Update user (e.g., change role)
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User update) {
        return userRepository.findById(id).map(u -> {
            u.setFullname(update.getFullname());
            u.setEmail(update.getEmail());
            u.setPhone(update.getPhone());
            u.setAddress(update.getAddress());
            u.setAvatarUrl(update.getAvatarUrl());
            u.setRole(update.getRole());
            userRepository.save(u);
            return ResponseEntity.ok(u);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete user
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            userRepository.delete(u);
            return ResponseEntity.ok("Deleted");
        }).orElse(ResponseEntity.notFound().build());
    }
}


