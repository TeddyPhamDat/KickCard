package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

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

    public String getTmnCode() {
        return tmnCode;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }
}