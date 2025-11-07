package com.example.backend.controller;

import com.example.backend.dto.PaymentResponse;
import com.example.backend.dto.TopupRequest;
import com.example.backend.model.User;
import com.example.backend.model.WalletTransaction;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletTransactionRepository;
import com.example.backend.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PaymentController - Quản lý tất cả endpoints thanh toán
 *
 * Luồng thanh toán:
 * 1. POST /api/payments/create -> Tạo payment request gửi tới PayOS
 * 2. PayOS trả về payment URL
 * 3. Client mở URL trong browser để user thanh toán
 * 4. PayOS gọi webhook khi payment thành công
 * 5. GET /api/payments/status/{orderCode} -> Check trạng thái payment
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    // Test endpoint to check if ngrok is working
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is running! Time: " + new java.util.Date());
    }

    /*
    /**
     * Tạo payment request cho mua thẻ
     */
    /*@PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody com.example.backend.dto.CreatePaymentRequest request) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return unauthorized("Chưa đăng nhập");
            }

            // Validate request
            if (request.amount == null || request.amount <= 0) {
                return badRequest("Số tiền phải lớn hơn 0");
            }
            if (request.cardId == null || request.cardId <= 0) {
                return badRequest("Card ID không hợp lệ");
            }

            // Tạo payment qua PayOS
            PaymentResponse payment = payOSService.createCardPayment(
                request.cardId,
                request.amount,
                request.description,
                user.getId()
            );

            return success("Payment created successfully", Map.of(
                "payment", payment,
                "checkoutUrl", payment.paymentUrl
            ));

        } catch (Exception e) {
            return error("Lỗi tạo payment: " + e.getMessage());
        }
    }*/

    /*
    /**
     * Lấy thông tin payment theo order code
     */
    /*@GetMapping("/{orderCode}")
    public ResponseEntity<?> getPayment(@PathVariable String orderCode) {
        // Commented out PayOS implementation
        return error("PayOS endpoints disabled");
    }

    /**
     * Kiểm tra trạng thái payment - check với PayOS API thật và cập nhật database
     */
    /*@GetMapping("/status/{orderCode}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String orderCode) {
        // Commented out PayOS implementation
        return error("PayOS endpoints disabled");
    }

    /**
     * Lấy danh sách payment của user
     */
    /*@GetMapping("/my-purchases")
    public ResponseEntity<?> getMyPurchases() {
        // Commented out PayOS implementation
        return error("PayOS endpoints disabled");
    }*/

    /*
    /**
     * Webhook từ PayOS khi payment hoàn tất
     * Endpoint này KHÔNG cần authentication vì được gọi từ PayOS server
     */
    /*@PostMapping("/webhook")
    public ResponseEntity<?> handlePaymentWebhook(@RequestBody(required = false) Map<String, Object> webhookData) {
        // Commented out PayOS webhook implementation
        return ResponseEntity.ok(Map.of("success", false, "message", "PayOS disabled"));
    }*/

    /**
     * Test endpoint để kiểm tra webhook có accessible không
     */
//    @GetMapping("/webhook/test")
//    public ResponseEntity<?> testWebhook() {
//        System.out.println("[PaymentController] Webhook test endpoint called at: " + java.time.LocalDateTime.now());
//        return ResponseEntity.ok(Map.of(
//            "success", true,
//            "message", "Webhook endpoint is accessible",
//            "timestamp", java.time.LocalDateTime.now().toString(),
//            "server", "KickCard Backend",
//            "webhookUrl", "https://kickcard.onrender.com/api/payments/webhook"
//        ));
//    }

    /*
    /**
     * Test endpoint để simulate webhook call (for testing)
     */
    /*@PostMapping("/webhook/simulate")
    public ResponseEntity<?> simulateWebhook() {
        return ResponseEntity.ok(Map.of("success", false, "message", "PayOS disabled"));
    }

    /**
     * Debug endpoint - Kiểm tra config PayOS
     */
    /*@GetMapping("/debug/config")
    public ResponseEntity<?> debugPayOSConfig() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "VNPay Configuration Active",
            "status", "PayOS disabled, VNPay enabled"
        ));
    }*/

    // ==================== NẠP TIỀN VÀO VÍ ====================

    /**
     * Lấy lịch sử giao dịch ví của user hiện tại
     */
    @GetMapping("/wallet-transactions")
    public ResponseEntity<?> getWalletTransactions() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return unauthorized("Chưa đăng nhập");
            }

            List<WalletTransaction> transactions = walletTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

            // Format transactions for response
            List<Map<String, Object>> formattedTransactions = transactions.stream()
                .map(tx -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", tx.getId());
                    map.put("orderCode", tx.getOrderCode());
                    map.put("amount", tx.getAmount());
                    map.put("type", tx.getType());
                    map.put("status", tx.getStatus());
                    map.put("paymentMethod", tx.getPaymentMethod());
                    map.put("description", tx.getDescription());
                    map.put("transactionNo", tx.getTransactionNo());
                    map.put("bankCode", tx.getBankCode());
                    map.put("createdAt", tx.getCreatedAt().toString());
                    map.put("completedAt", tx.getCompletedAt() != null ? tx.getCompletedAt().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());

            return success("Wallet transactions retrieved successfully", Map.of(
                "transactions", formattedTransactions,
                "total", transactions.size()
            ));

        } catch (Exception e) {
            System.err.println("[Wallet Transactions] Error: " + e.getMessage());
            return error("Lỗi lấy lịch sử giao dịch: " + e.getMessage());
        }
    }

    /**
     * Nạp tiền THẬT vào ví qua VNPay (tạo payment link)
     * Được gọi bởi WalletFragment khi user chọn số tiền và nhấn "Nạp Tiền"
     */
    @PostMapping("/topup")
    public ResponseEntity<?> topupWalletReal(@RequestBody TopupRequest request,
                                           HttpServletRequest httpRequest) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return unauthorized("Chưa đăng nhập");
            }

            if (request.getAmount() == null || request.getAmount() <= 0) {
                return badRequest("Số tiền nạp phải lớn hơn 0");
            }

            // Validate amount range (tối thiểu 10,000 VND, tối đa 10,000,000 VND)
            if (request.getAmount() < 10000) {
                return badRequest("Số tiền nạp tối thiểu là 10,000 VND");
            }
            if (request.getAmount() > 10000000) {
                return badRequest("Số tiền nạp tối đa là 10,000,000 VND");
            }

            // Tạo orderCode unique với random string để tránh trùng lặp
            String orderCode = "TOPUP_" + user.getId() + "_" + System.currentTimeMillis() + "_" + 
                              (int)(Math.random() * 10000);
            String orderInfo = "Nap tien vi KickCard - User " + user.getId();
            
            System.out.println("[VNPay] Creating payment with orderCode: " + orderCode + ", amount: " + request.getAmount());
            
            // Save wallet transaction as PENDING
            WalletTransaction transaction = new WalletTransaction();
            transaction.setUserId(user.getId());
            transaction.setOrderCode(orderCode);
            transaction.setAmount(request.getAmount());
            transaction.setType("TOPUP");
            transaction.setStatus("PENDING");
            transaction.setPaymentMethod("VNPAY");
            transaction.setDescription(orderInfo);
            transaction.setCreatedAt(LocalDateTime.now());
            walletTransactionRepository.save(transaction);
            
            // Get client IP
            String clientIP = getClientIP(httpRequest);
            
            // Tạo payment URL với VNPay
            String paymentUrl = vnPayService.createPaymentUrl(
                orderCode, 
                request.getAmount().longValue(), 
                orderInfo, 
                clientIP
            );

            // Lưu thông tin payment vào database (tạm thời comment)
            // PaymentResponse payment = savePaymentRecord(orderCode, request.getAmount(), user.getId(), "TOPUP");

            return success("Topup created successfully", Map.of(
                "checkoutUrl", paymentUrl,
                "orderCode", orderCode,
                "amount", request.getAmount()
            ));

        } catch (Exception e) {
            System.err.println("[PaymentController] Topup error: " + e.getMessage());
            return error("Lỗi tạo request nạp tiền: " + e.getMessage());
        }
    }

    /*
    /**
     * Lấy danh sách nạp tiền của user
     */
    /*@GetMapping("/my-topups")
    public ResponseEntity<?> getMyTopups() {
        // Commented out PayOS implementation
        return error("PayOS endpoints disabled");
    }*/

    // ==================== VNPAY ENDPOINTS ====================

    /**
     * Check payment status - for polling from mobile app
     */
    @GetMapping("/status/{orderCode}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String orderCode) {
        try {
            // For demo purposes, we'll simulate payment completion after 30 seconds
            // In real implementation, you would check with VNPay API or database
            
            // Extract timestamp from orderCode
            if (orderCode.startsWith("TOPUP_")) {
                String[] parts = orderCode.split("_");
                if (parts.length >= 3) {
                    long timestamp = Long.parseLong(parts[2]);
                    long currentTime = System.currentTimeMillis();
                    long elapsed = currentTime - timestamp;
                    
                    // Simulate payment completion after 30 seconds
                    if (elapsed > 30000) {
                        return success("Payment completed", Map.of(
                            "status", "COMPLETED",
                            "orderCode", orderCode,
                            "message", "Payment completed successfully"
                        ));
                    } else {
                        return success("Payment pending", Map.of(
                            "status", "PENDING",
                            "orderCode", orderCode,
                            "message", "Payment is being processed",
                            "remainingTime", (30000 - elapsed) / 1000
                        ));
                    }
                }
            }
            
            return error("Invalid order code format");
            
        } catch (Exception e) {
            System.err.println("[Payment Status] Error: " + e.getMessage());
            return error("Failed to check payment status: " + e.getMessage());
        }
    }

    /**
     * Manual complete payment - for testing purposes
     */
    @PostMapping("/complete/{orderCode}")
    public ResponseEntity<?> completePayment(@PathVariable String orderCode) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Extract amount from a payment record or simulate
            // For demo, we'll add 50,000 VND to wallet
            double topupAmount = 50000.0;
            
            user.setWalletBalance(user.getWalletBalance() + topupAmount);
            userRepository.save(user);

            return success("Payment completed manually", Map.of(
                "status", "COMPLETED",
                "orderCode", orderCode,
                "amount", topupAmount,
                "newBalance", user.getWalletBalance(),
                "message", "Wallet balance updated successfully"
            ));

        } catch (Exception e) {
            System.err.println("[Complete Payment] Error: " + e.getMessage());
            return error("Failed to complete payment: " + e.getMessage());
        }
    }

    /**
     * VNPay Return URL - User được redirect về sau khi thanh toán
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> handleVNPayReturn(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Set headers to bypass ngrok warning
            response.setHeader("ngrok-skip-browser-warning", "true");
            
            Map<String, String> params = new HashMap<>();
            for (String key : request.getParameterMap().keySet()) {
                params.put(key, request.getParameter(key));
            }

            System.out.println("[VNPay Return] Received params: " + params);

            // Validate signature first
            boolean isValidSignature = vnPayService.validateSignature(new HashMap<>(params));
            if (!isValidSignature) {
                System.err.println("[VNPay Return] Invalid signature!");
                return ResponseEntity.ok(generateReturnHtml(
                    "Lỗi xác thực",
                    "Chữ ký giao dịch không hợp lệ",
                    "Vui lòng liên hệ hỗ trợ nếu bạn đã thanh toán.",
                    "#dc3545"
                ));
            }

            String responseCode = params.get("vnp_ResponseCode");
            String orderCode = params.get("vnp_TxnRef");
            String amount = params.get("vnp_Amount");
            String bankCode = params.get("vnp_BankCode");
            String transactionNo = params.get("vnp_TransactionNo");

            // Create deep link URL with parameters
            String deepLinkUrl;
            String statusMessage;
            String statusColor;
            
            if ("00".equals(responseCode)) {
                // Payment thành công - CẬP NHẬT VÍ
                long amountVND = Long.parseLong(amount) / 100; // VNPay trả về amount * 100

                // Extract userId from orderCode (format: TOPUP_userId_timestamp_random)
                try {
                    String[] parts = orderCode.split("_");
                    if (parts.length >= 2 && parts[0].equals("TOPUP")) {
                        Long userId = Long.parseLong(parts[1]);
                        User user = userRepository.findById(userId).orElse(null);

                        if (user != null) {
                            // Cộng tiền vào ví
                            double newBalance = user.getWalletBalance() + amountVND;
                            user.setWalletBalance(newBalance);
                            userRepository.save(user);

                            // Update wallet transaction status
                            walletTransactionRepository.findByOrderCode(orderCode).ifPresent(tx -> {
                                tx.setStatus("COMPLETED");
                                tx.setCompletedAt(LocalDateTime.now());
                                tx.setTransactionNo(transactionNo);
                                tx.setBankCode(bankCode);
                                tx.setUpdatedAt(LocalDateTime.now());
                                walletTransactionRepository.save(tx);
                            });

                            System.out.println("[VNPay] ✅ Updated wallet for user " + userId +
                                             " - Added: " + amountVND + " - New balance: " + newBalance);
                        } else {
                            System.err.println("[VNPay] User not found: " + userId);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[VNPay] Error updating wallet: " + e.getMessage());
                }

                deepLinkUrl = String.format("kickcard://payment/success?orderCode=%s&amount=%d&responseCode=%s&transactionNo=%s",
                    orderCode, amountVND, responseCode, transactionNo);
                statusMessage = "✅ Nạp tiền thành công!";
                statusColor = "#28a745";
            } else {
                // Payment thất bại - Update transaction status
                walletTransactionRepository.findByOrderCode(orderCode).ifPresent(tx -> {
                    tx.setStatus("FAILED");
                    tx.setUpdatedAt(LocalDateTime.now());
                    walletTransactionRepository.save(tx);
                });
                
                deepLinkUrl = String.format("kickcard://payment/cancel?orderCode=%s&responseCode=%s", 
                    orderCode, responseCode);
                statusMessage = "❌ Thanh toán thất bại";
                statusColor = "#dc3545";
            }

            // Generate HTML that tries deep link first, then shows success page
            String redirectHtml = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>KickCard Payment Result</title>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; text-align: center; }
                        .container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 400px; margin: 50px auto; }
                        .status { color: %s; font-size: 24px; margin-bottom: 20px; font-weight: bold; }
                        .info { color: #666; margin: 10px 0; font-size: 14px; }
                        .amount { color: #28a745; font-size: 32px; font-weight: bold; margin: 20px 0; }
                        .button { background: #007bff; color: white; padding: 12px 24px; border: none; border-radius: 5px; text-decoration: none; display: inline-block; margin: 10px; }
                        .button:hover { background: #0056b3; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="status">%s</div>
                        %s
                        <div class="info">Mã giao dịch: %s</div>
                        <div class="info">Ngân hàng: %s</div>
                        <div class="info">Thời gian: %s</div>
                        <br>
                        <p id="message">Đang chuyển hướng về ứng dụng...</p>
                        <a href="%s" class="button">Mở ứng dụng KickCard</a>
                        <br><br>
                        <small>Nếu ứng dụng không tự động mở, hãy click vào nút trên.</small>
                    </div>
                    <script>
                        // Try to redirect to app immediately
                        window.location.href = "%s";
                        
                        // Show success message after 2 seconds if deep link didn't work
                        setTimeout(function() {
                            document.getElementById('message').textContent = 'Vui lòng mở ứng dụng KickCard để xem kết quả.';
                        }, 2000);
                    </script>
                </body>
                </html>
                """, 
                statusColor,
                statusMessage,
                "00".equals(responseCode) ? String.format("<div class=\"amount\">+%,d VND</div>", Long.parseLong(amount) / 100) : "",
                orderCode,
                bankCode != null ? bankCode : "N/A",
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()),
                deepLinkUrl,
                deepLinkUrl);

            return ResponseEntity.ok(redirectHtml);

        } catch (Exception e) {
            System.err.println("[VNPay Return] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(generateReturnHtml(
                "Lỗi hệ thống",
                "Không thể xử lý thông tin giao dịch",
                "Vui lòng kiểm tra lại trong app hoặc liên hệ hỗ trợ.",
                "#dc3545"
            ));
        }
    }

    /**
     * VNPay Notify URL - VNPay gọi để thông báo kết quả thanh toán
     */
    @PostMapping("/vnpay-notify")
    public ResponseEntity<?> handleVNPayNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            for (String key : request.getParameterMap().keySet()) {
                params.put(key, request.getParameter(key));
            }

            System.out.println("[VNPay Notify] Received params: " + params);

            // Validate signature
            boolean isValidSignature = vnPayService.validateSignature(params);
            if (!isValidSignature) {
                System.err.println("[VNPay Notify] Invalid signature!");
                return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid signature"));
            }

            String responseCode = params.get("vnp_ResponseCode");
            String orderCode = params.get("vnp_TxnRef");
            String amount = params.get("vnp_Amount");
            String bankCode = params.get("vnp_BankCode");
            String payDate = params.get("vnp_PayDate");

            if ("00".equals(responseCode)) {
                // Payment thành công - cập nhật database
                System.out.println("[VNPay Notify] Payment successful: " + orderCode);
                
                // TODO: Cập nhật wallet balance cho user
                // updateUserWallet(orderCode, amount);
                
                return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Success"));
            } else {
                System.out.println("[VNPay Notify] Payment failed: " + orderCode + ", code: " + responseCode);
                return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Payment failed"));
            }

        } catch (Exception e) {
            System.err.println("[VNPay Notify] Error: " + e.getMessage());
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "System error"));
        }
    }

    /*
    /**
     * Return URL - Xử lý khi user thanh toán thành công trên PayOS và được redirect về
     */
    /*@GetMapping("/return")
    public ResponseEntity<String> handlePaymentReturn(@RequestParam(required = false) String orderCode) {
        return ResponseEntity.ok(generateReturnHtml(
            "PayOS Disabled",
            "PayOS endpoints have been disabled",
            "Please use VNPay integration instead",
            "#6c757d"
        ));
    }

    /**
     * Cancel URL - Xử lý khi user hủy thanh toán trên PayOS
     */
    /*@GetMapping("/cancel")
    public ResponseEntity<String> handlePaymentCancel(@RequestParam(required = false) String orderCode) {
        return ResponseEntity.ok(generateReturnHtml(
            "PayOS Disabled",
            "PayOS endpoints have been disabled",
            "Please use VNPay integration instead",
            "#6c757d"
        ));
    }*/

    /**
     * Trang success chung để test
     */
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam(required = false) String orderCode) {
        return ResponseEntity.ok(generateReturnHtml(
            "Giao dịch thành công! 🎉",
            "Cảm ơn bạn đã sử dụng KickCard",
            orderCode != null ? "Mã đơn hàng: " + orderCode : "Bạn có thể đóng tab này và quay lại app.",
            "#28a745"
        ));
    }

    // ==================== HELPER METHODS ====================

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private ResponseEntity<?> success(String message) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", message
        ));
    }

    private ResponseEntity<?> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data instanceof Map) {
            response.putAll((Map<String, Object>) data);
        } else {
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> badRequest(String error) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", error
        ));
    }

    private ResponseEntity<?> unauthorized(String error) {
        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "error", error
        ));
    }

    private ResponseEntity<?> forbidden(String error) {
        return ResponseEntity.status(403).body(Map.of(
            "success", false,
            "error", error
        ));
    }

    private ResponseEntity<?> error(String error) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", error
        ));
    }

    /**
     * Generate HTML page for return/cancel URLs
     */
    private String generateReturnHtml(String title, String message, String details, String color) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>KickCard - %s</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        margin: 0;
                        padding: 20px;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 16px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                        text-align: center;
                        max-width: 500px;
                        width: 100%%;
                    }
                    .icon {
                        font-size: 64px;
                        margin-bottom: 20px;
                        color: %s;
                    }
                    .title {
                        font-size: 28px;
                        font-weight: bold;
                        color: %s;
                        margin-bottom: 16px;
                    }
                    .message {
                        font-size: 18px;
                        color: #333;
                        margin-bottom: 20px;
                        font-weight: 500;
                    }
                    .details {
                        font-size: 14px;
                        color: #666;
                        line-height: 1.6;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 24px;
                        font-weight: bold;
                        color: #667eea;
                        margin-top: 20px;
                    }
                    .close-btn {
                        background: %s;
                        color: white;
                        border: none;
                        padding: 12px 24px;
                        border-radius: 8px;
                        font-size: 16px;
                        cursor: pointer;
                        margin-top: 20px;
                    }
                    .close-btn:hover {
                        opacity: 0.9;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="title">%s</div>
                    <div class="message">%s</div>
                    <div class="details">%s</div>
                    <button class="close-btn" onclick="window.close()">Đóng tab này</button>
                    <div class="logo">⚽ KickCard</div>
                </div>
                <script>
                    // Auto close tab after 10 seconds
                    setTimeout(() => {
                        window.close();
                    }, 10000);
                </script>
            </body>
            </html>
            """.formatted(title, color, color, color, title, message, details);
    }

    /*
    /**
     * Test webhook endpoint - để test local
     */
    /*@PostMapping("/webhook/test/{orderCode}")
    public ResponseEntity<?> testWebhook(@PathVariable String orderCode) {
        return error("PayOS endpoints disabled");
    }

    /**
     * Force check payment status từ PayOS API
     */
    /*@PostMapping("/force-check/{orderCode}")
    public ResponseEntity<?> forceCheckPayment(@PathVariable String orderCode) {
        return error("PayOS endpoints disabled");
    }*/

    /**
     * Format amount for display
     */
    private String formatAmount(Double amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }

    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
