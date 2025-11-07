//package com.example.backend.controller;
//
//import com.example.backend.dto.MyCardDTO;
//import com.example.backend.model.Card;
//import com.example.backend.repository.CardRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/admin-cards")
//public class AdminCardsController {
//    private final CardRepository cardRepository;
//
//    public AdminCardsController(CardRepository cardRepository) {
//        this.cardRepository = cardRepository;
//    }
//
//    @GetMapping("/pending")
//    public ResponseEntity<?> getPendingCards() {
//        return getPendingCardsByOwnerId(null);
//    }
//
//    @GetMapping("/pending/by-owner")
//    public ResponseEntity<?> getPendingCardsByOwnerId(@RequestParam(value = "ownerId", required = false) Long ownerId) {
//        List<Card> cards = cardRepository.findByStatus("PENDING");
//        if (ownerId != null) {
//            cards = cards.stream().filter(c -> ownerId.equals(c.getOwnerId())).collect(Collectors.toList());
//        }
//        List<MyCardDTO> dtos = cards.stream().map(c -> {
//            MyCardDTO d = new MyCardDTO();
//            d.id = c.getId();
//            d.name = c.getName();
//            d.rarity = c.getRarity();
//            d.team = c.getTeam();
//            d.description = c.getDescription();
//            d.baseImageUrl = c.getBaseImageUrl();
//            d.status = c.getStatus();
//            d.rejectionReason = c.getRejectionReason();
//            d.ownerId = c.getOwnerId();
//            d.price = c.getPrice();
//            return d;
//        }).collect(Collectors.toList());
//        return ResponseEntity.ok(dtos);
//    }
//
//    @GetMapping("/rejected")
//    public ResponseEntity<?> getRejectedCards() {
//        return getRejectedCardsByOwnerId(null);
//    }
//
//    @GetMapping("/rejected/by-owner")
//    public ResponseEntity<?> getRejectedCardsByOwnerId(@RequestParam(value = "ownerId", required = false) Long ownerId) {
//        List<Card> cards = cardRepository.findByStatus("REJECTED");
//        if (ownerId != null) {
//            cards = cards.stream().filter(c -> ownerId.equals(c.getOwnerId())).collect(Collectors.toList());
//        }
//        List<MyCardDTO> dtos = cards.stream().map(c -> {
//            MyCardDTO d = new MyCardDTO();
//            d.id = c.getId();
//            d.name = c.getName();
//            d.rarity = c.getRarity();
//            d.team = c.getTeam();
//            d.description = c.getDescription();
//            d.baseImageUrl = c.getBaseImageUrl();
//            d.status = c.getStatus();
//            d.rejectionReason = c.getRejectionReason();
//            d.ownerId = c.getOwnerId();
//            d.price = c.getPrice();
//            return d;
//        }).collect(Collectors.toList());
//        return ResponseEntity.ok(dtos);
//    }
//}
