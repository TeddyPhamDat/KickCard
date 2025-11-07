package com.example.backend.controller;

import com.example.backend.dto.MyCardDTO;
import com.example.backend.model.Card;
import com.example.backend.model.User;
import com.example.backend.repository.CardRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/my-cards")
@CrossOrigin(origins = "*")
public class MyCardsController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public MyCardsController(CardRepository cardRepository, UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // POST method chính - tự động detect content type
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MyCardDTO dto) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        try {
            Card c = new Card();
            c.setName(dto.name);
            c.setRarity(dto.rarity);
            c.setTeam(dto.team);
            c.setDescription(dto.description);
            c.setPrice(dto.price);
            c.setBaseImageUrl(dto.baseImageUrl);
            c.setOwnerId(u.getId());
            c.setStatus("PENDING");

            Card saved = cardRepository.save(c);

            // Tạo DTO response
            MyCardDTO responseDto = new MyCardDTO();
            responseDto.id = saved.getId();
            responseDto.name = saved.getName();
            responseDto.rarity = saved.getRarity();
            responseDto.team = saved.getTeam();
            responseDto.description = saved.getDescription();
            responseDto.baseImageUrl = saved.getBaseImageUrl();
            responseDto.price = saved.getPrice();
            responseDto.status = saved.getStatus();
            responseDto.rejectionReason = saved.getRejectionReason();

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi tạo thẻ: " + e.getMessage());
        }
    }

    // POST method để tạo thẻ với file upload
    @PostMapping("/with-image")
    public ResponseEntity<?> createWithFile(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "team", required = false) String team,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        try {
            Card c = new Card();
            c.setName(name);
            c.setRarity(rarity);
            c.setTeam(team);
            c.setDescription(description);
            c.setPrice(price);
            c.setOwnerId(u.getId());
            c.setStatus("PENDING");

            // Upload hình ảnh nếu có
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                c.setBaseImageUrl(imageUrl);
            }

            Card saved = cardRepository.save(c);

            // Tạo DTO response
            MyCardDTO dto = new MyCardDTO();
            dto.id = saved.getId();
            dto.name = saved.getName();
            dto.rarity = saved.getRarity();
            dto.team = saved.getTeam();
            dto.description = saved.getDescription();
            dto.baseImageUrl = saved.getBaseImageUrl();
            dto.price = saved.getPrice();
            dto.status = saved.getStatus();
            dto.rejectionReason = saved.getRejectionReason();

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi tạo thẻ: " + e.getMessage());
        }
    }

    // PUT method chính
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MyCardDTO dto) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            try {
                if (!u.getId().equals(card.getOwnerId())) {
                    return ResponseEntity.status(403).body("Forbidden");
                }

                String status = card.getStatus();
                if (!"PENDING".equalsIgnoreCase(status) && !"APPROVED".equalsIgnoreCase(status) && !"SOLD".equalsIgnoreCase(status)) {
                    return ResponseEntity.badRequest().body("Only pending, approved, or sold cards can be edited");
                }

                // Cập nhật thông tin
                card.setName(dto.name);
                card.setRarity(dto.rarity);
                card.setTeam(dto.team);
                card.setDescription(dto.description);
                card.setPrice(dto.price);
                if (dto.baseImageUrl != null) {
                    card.setBaseImageUrl(dto.baseImageUrl);
                }

                // If card is APPROVED or SOLD, set status to PENDING for admin approval
                if ("APPROVED".equalsIgnoreCase(status) || "SOLD".equalsIgnoreCase(status)) {
                    card.setStatus("PENDING");
                }

                Card saved = cardRepository.save(card);

                // Tạo DTO response
                MyCardDTO responseDto = new MyCardDTO();
                responseDto.id = saved.getId();
                responseDto.name = saved.getName();
                responseDto.rarity = saved.getRarity();
                responseDto.team = saved.getTeam();
                responseDto.description = saved.getDescription();
                responseDto.baseImageUrl = saved.getBaseImageUrl();
                responseDto.price = saved.getPrice();
                responseDto.status = saved.getStatus();
                responseDto.rejectionReason = saved.getRejectionReason();

                return ResponseEntity.ok(responseDto);

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Lỗi cập nhật thẻ: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT method để cập nhật thẻ với file upload
    @PutMapping("/{id}/with-image")
    public ResponseEntity<?> updateWithFile(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rarity", required = false) String rarity,
            @RequestParam(value = "team", required = false) String team,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            try {
                if (!u.getId().equals(card.getOwnerId())) {
                    return ResponseEntity.status(403).body("Forbidden");
                }

                String status = card.getStatus();
                if (!"PENDING".equalsIgnoreCase(status) && !"APPROVED".equalsIgnoreCase(status) && !"SOLD".equalsIgnoreCase(status)) {
                    return ResponseEntity.badRequest().body("Only pending, approved, or sold cards can be edited");
                }

                // Cập nhật thông tin cơ bản
                if (name != null) card.setName(name);
                if (rarity != null) card.setRarity(rarity);
                if (team != null) card.setTeam(team);
                if (description != null) card.setDescription(description);
                if (price != null) card.setPrice(price);

                // Xử lý hình ảnh nếu có
                if (file != null && !file.isEmpty()) {
                    String newImageUrl;

                    // Nếu có ảnh cũ, xóa và upload ảnh mới
                    if (card.getBaseImageUrl() != null && !card.getBaseImageUrl().isEmpty()) {
                        try {
                            String oldPublicId = cloudinaryService.extractPublicId(card.getBaseImageUrl());
                            newImageUrl = cloudinaryService.updateImage(file, oldPublicId);
                        } catch (Exception e) {
                            // Nếu không extract được publicId, chỉ upload ảnh mới
                            newImageUrl = cloudinaryService.uploadImage(file);
                        }
                    } else {
                        // Nếu chưa có ảnh, upload mới
                        newImageUrl = cloudinaryService.uploadImage(file);
                    }

                    card.setBaseImageUrl(newImageUrl);
                }

                // If card is APPROVED or SOLD, set status to PENDING for admin approval
                if ("APPROVED".equalsIgnoreCase(status) || "SOLD".equalsIgnoreCase(status)) {
                    card.setStatus("PENDING");
                }

                Card saved = cardRepository.save(card);

                // Tạo DTO response
                MyCardDTO dto = new MyCardDTO();
                dto.id = saved.getId();
                dto.name = saved.getName();
                dto.rarity = saved.getRarity();
                dto.team = saved.getTeam();
                dto.description = saved.getDescription();
                dto.baseImageUrl = saved.getBaseImageUrl();
                dto.price = saved.getPrice();
                dto.status = saved.getStatus();
                dto.rejectionReason = saved.getRejectionReason();

                return ResponseEntity.ok(dto);

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Lỗi cập nhật thẻ: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            try {
                if (!u.getId().equals(card.getOwnerId())) {
                    return ResponseEntity.status(403).body("Forbidden");
                }

                if (!"PENDING".equalsIgnoreCase(card.getStatus())) {
                    return ResponseEntity.badRequest().body("Only pending cards can be deleted");
                }

                // Xóa hình ảnh trên Cloudinary nếu có
                if (card.getBaseImageUrl() != null && !card.getBaseImageUrl().isEmpty()) {
                    try {
                        String publicId = cloudinaryService.extractPublicId(card.getBaseImageUrl());
                        cloudinaryService.deleteImage(publicId);
                    } catch (Exception e) {
                        // Log error nhưng vẫn tiếp tục xóa card
                        System.err.println("Không thể xóa ảnh trên Cloudinary: " + e.getMessage());
                    }
                }

                cardRepository.delete(card);
                return ResponseEntity.ok(Map.of("deleted", true, "message", "Xóa thẻ thành công"));

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Lỗi xóa thẻ: " + e.getMessage());
            }
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

    /**
     * API để resell thẻ - đưa thẻ đã mua (SOLD) trở lại marketplace
     */
    @PostMapping("/{id}/resell")
    public ResponseEntity<?> resellCard(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            try {
                // Kiểm tra quyền sở hữu
                if (!u.getId().equals(card.getOwnerId())) {
                    return ResponseEntity.status(403).body("Bạn không có quyền resell thẻ này");
                }

                // Chỉ cho phép resell thẻ có status SOLD (đã mua)
                if (!"SOLD".equalsIgnoreCase(card.getStatus())) {
                    return ResponseEntity.badRequest().body("Chỉ có thể resell thẻ đã mua (status SOLD)");
                }

                // Lấy giá mới từ request (optional)
                Double newPrice = null;
                if (request.containsKey("price") && request.get("price") != null) {
                    Object priceObj = request.get("price");
                    if (priceObj instanceof Number) {
                        newPrice = ((Number) priceObj).doubleValue();
                    } else if (priceObj instanceof String) {
                        try {
                            newPrice = Double.parseDouble((String) priceObj);
                        } catch (NumberFormatException e) {
                            return ResponseEntity.badRequest().body("Giá không hợp lệ");
                        }
                    }

                    if (newPrice <= 0) {
                        return ResponseEntity.badRequest().body("Giá phải lớn hơn 0");
                    }
                }

                // Cập nhật thẻ để resell
                if (newPrice != null) {
                    card.setPrice(newPrice);
                }
                card.setStatus("PENDING"); // Đưa về PENDING để admin duyệt lại
                card.setRejectionReason(null); // Xóa lý do từ chối cũ

                Card saved = cardRepository.save(card);

                // Tạo DTO response
                MyCardDTO responseDto = new MyCardDTO();
                responseDto.id = saved.getId();
                responseDto.name = saved.getName();
                responseDto.rarity = saved.getRarity();
                responseDto.team = saved.getTeam();
                responseDto.description = saved.getDescription();
                responseDto.baseImageUrl = saved.getBaseImageUrl();
                responseDto.price = saved.getPrice();
                responseDto.status = saved.getStatus();
                responseDto.rejectionReason = saved.getRejectionReason();

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thẻ đã được đưa lên marketplace để resell. Đang chờ admin duyệt.",
                    "card", responseDto
                ));

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Lỗi resell thẻ: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * API để lấy danh sách thẻ có thể resell (status SOLD)
     */
    @GetMapping("/resellable")
    public ResponseEntity<?> getResellableCards() {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        List<Card> resellableCards = cardRepository.findAll().stream()
            .filter(c -> u.getId().equals(c.getOwnerId()) && "SOLD".equalsIgnoreCase(c.getStatus()))
            .collect(Collectors.toList());

        List<MyCardDTO> dtos = resellableCards.stream().map(c -> {
            MyCardDTO d = new MyCardDTO();
            d.id = c.getId();
            d.name = c.getName();
            d.rarity = c.getRarity();
            d.team = c.getTeam();
            d.description = c.getDescription();
            d.baseImageUrl = c.getBaseImageUrl();
            d.status = c.getStatus();
            d.price = c.getPrice();
            return d;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "resellableCards", dtos,
            "count", dtos.size()
        ));
    }

    /**
     * API để lấy danh sách thẻ resell bị reject (status REJECTED và đã từng có status SOLD)
     */
    @GetMapping("/rejected-resells")
    public ResponseEntity<?> getRejectedResells() {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        // Lấy tất cả thẻ REJECTED của user (bao gồm cả thẻ resell bị reject)
        List<Card> rejectedCards = cardRepository.findAll().stream()
            .filter(c -> u.getId().equals(c.getOwnerId()) && "REJECTED".equalsIgnoreCase(c.getStatus()))
            .collect(Collectors.toList());

        List<MyCardDTO> dtos = rejectedCards.stream().map(c -> {
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

        return ResponseEntity.ok(Map.of(
            "rejectedResells", dtos,
            "count", dtos.size()
        ));
    }

    /**
     * API để resell lại thẻ đã bị reject
     */
    @PostMapping("/{id}/retry-resell")
    public ResponseEntity<?> retryResell(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            try {
                // Kiểm tra quyền sở hữu
                if (!u.getId().equals(card.getOwnerId())) {
                    return ResponseEntity.status(403).body("Bạn không có quyền retry resell thẻ này");
                }

                // Chỉ cho phép retry resell thẻ có status REJECTED
                if (!"REJECTED".equalsIgnoreCase(card.getStatus())) {
                    return ResponseEntity.badRequest().body("Chỉ có thể retry resell thẻ bị reject (status REJECTED)");
                }

                // Lấy giá mới từ request (optional)
                Double newPrice = null;
                if (request.containsKey("price") && request.get("price") != null) {
                    Object priceObj = request.get("price");
                    if (priceObj instanceof Number) {
                        newPrice = ((Number) priceObj).doubleValue();
                    } else if (priceObj instanceof String) {
                        try {
                            newPrice = Double.parseDouble((String) priceObj);
                        } catch (NumberFormatException e) {
                            return ResponseEntity.badRequest().body("Giá không hợp lệ");
                        }
                    }

                    if (newPrice <= 0) {
                        return ResponseEntity.badRequest().body("Giá phải lớn hơn 0");
                    }
                }

                // Cập nhật thẻ để retry resell
                if (newPrice != null) {
                    card.setPrice(newPrice);
                }
                card.setStatus("PENDING"); // Đưa về PENDING để admin duyệt lại
                card.setRejectionReason(null); // Xóa lý do từ chối cũ

                Card saved = cardRepository.save(card);

                // Tạo DTO response
                MyCardDTO responseDto = new MyCardDTO();
                responseDto.id = saved.getId();
                responseDto.name = saved.getName();
                responseDto.rarity = saved.getRarity();
                responseDto.team = saved.getTeam();
                responseDto.description = saved.getDescription();
                responseDto.baseImageUrl = saved.getBaseImageUrl();
                responseDto.price = saved.getPrice();
                responseDto.status = saved.getStatus();
                responseDto.rejectionReason = saved.getRejectionReason();

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thẻ đã được gửi lại để admin duyệt resell. Đang chờ admin duyệt.",
                    "card", responseDto
                ));

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Lỗi retry resell thẻ: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * API để hủy resell và đưa thẻ về trạng thái SOLD (chỉ dành cho thẻ bị reject)
     */
    @PostMapping("/{id}/cancel-resell")
    public ResponseEntity<?> cancelResell(@PathVariable Long id) {
        User u = getCurrentUser();
        if (u == null) return ResponseEntity.status(401).body("Unauthorized");

        return cardRepository.findById(id).map(card -> {
            try {
                // Kiểm tra quyền sở hữu
                if (!u.getId().equals(card.getOwnerId())) {
                    return ResponseEntity.status(403).body("Bạn không có quyền cancel resell thẻ này");
                }

                // Chỉ cho phép cancel resell thẻ có status REJECTED hoặc PENDING (đang trong quá trình resell)
                if (!"REJECTED".equalsIgnoreCase(card.getStatus()) && !"PENDING".equalsIgnoreCase(card.getStatus())) {
                    return ResponseEntity.badRequest().body("Chỉ có thể cancel resell thẻ đang trong quá trình resell (status REJECTED hoặc PENDING)");
                }

                // Đưa thẻ về trạng thái SOLD (không bán nữa, chỉ sở hữu)
                card.setStatus("SOLD");
                card.setRejectionReason(null); // Xóa lý do từ chối

                Card saved = cardRepository.save(card);

                // Tạo DTO response
                MyCardDTO responseDto = new MyCardDTO();
                responseDto.id = saved.getId();
                responseDto.name = saved.getName();
                responseDto.rarity = saved.getRarity();
                responseDto.team = saved.getTeam();
                responseDto.description = saved.getDescription();
                responseDto.baseImageUrl = saved.getBaseImageUrl();
                responseDto.price = saved.getPrice();
                responseDto.status = saved.getStatus();
                responseDto.rejectionReason = saved.getRejectionReason();

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã hủy resell. Thẻ trở về trạng thái sở hữu (SOLD).",
                    "card", responseDto
                ));

            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Lỗi cancel resell thẻ: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}

