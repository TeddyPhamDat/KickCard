package com.example.assignment.data.repository;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.model.CreatePaymentRequest;
import com.example.assignment.data.model.PaymentResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class PaymentRepository {
    private ApiService apiService;

    public PaymentRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<Map<String, Object>> createPayment(String bearerToken, CreatePaymentRequest request) {
        return apiService.createPayment(bearerToken, request);
    }

    public Call<Map<String, Object>> createWalletPayment(String bearerToken, CreatePaymentRequest request) {
        return apiService.createPayment(bearerToken, request);
    }

    public Call<Map<String, Object>> createWalletTopUp(String bearerToken, com.example.assignment.data.model.TopupRequest request) {
        return apiService.createWalletTopUp(bearerToken, request);
    }

    public Call<Map<String, Object>> getPayment(String bearerToken, String orderCode) {
        return apiService.getPayment(bearerToken, orderCode);
    }

    public Call<Map<String, Object>> getMyPurchases(String bearerToken) {
        return apiService.getMyPurchases(bearerToken);
    }

    /*
    // Commented out since PayOS endpoints are disabled
    public Call<Map<String, Object>> getMySales(String bearerToken) {
        return apiService.getMySales(bearerToken);
    }

    public Call<Map<String, Object>> checkPaymentStatus(String bearerToken, String orderCode) {
        return apiService.checkPaymentStatus(bearerToken, orderCode);
    }

    public Call<Map<String, Object>> forceCheckPayment(String bearerToken, String orderCode) {
        return apiService.forceCheckPayment(bearerToken, orderCode);
    }
    */

    public Call<Map<String, Object>> getWalletBalance(String bearerToken) {
        return apiService.getWalletBalance(bearerToken);
    }

    public Call<Map<String, Object>> getWalletTransactions(String bearerToken) {
        return apiService.getWalletTransactions(bearerToken);
    }
}
