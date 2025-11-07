package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPayService {

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.secretKey}")
    private String secretKey;

    @Value("${vnpay.payUrl}")
    private String payUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    @Value("${vnpay.notifyUrl}")
    private String notifyUrl;

    public String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress) {
        Map<String, String> vnpParams = new TreeMap<>();
        
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay requires amount in cents
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = LocalDateTime.now().format(formatter);
        vnpParams.put("vnp_CreateDate", createDate);
        
        // Expire in 15 minutes
        String expireDate = LocalDateTime.now().plusMinutes(15).format(formatter);
        vnpParams.put("vnp_ExpireDate", expireDate);

        // Build query string
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            query.append("=");
            query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        // Generate hash
        String hashData = query.toString();
        String vnpSecureHash = hmacSHA512(secretKey, hashData);
        
        String paymentUrl = payUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
        
        return paymentUrl;
    }

    public boolean validateSignature(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // Sort parameters
        Map<String, String> sortedParams = new TreeMap<>(params);
        
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append("&");
            }
            hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            hashData.append("=");
            hashData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        String calculatedHash = hmacSHA512(secretKey, hashData.toString());
        return calculatedHash.equals(vnpSecureHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }
}