package com.example.assignment.data.repository;

import android.content.Context;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.utils.SessionManager;

import retrofit2.Call;

public class WalletRepository {
    private ApiService api;
    private SessionManager session;

    public WalletRepository(Context ctx, String baseUrl) {
        api = RetrofitClient.getClient(ctx, baseUrl).create(ApiService.class);
        session = new SessionManager(ctx);
    }

    private String bearer() {
        String t = session.fetchToken();
        return t == null ? null : "Bearer " + t;
    }

    public Call<java.util.Map<String, Object>> getBalance() { return api.getWalletBalance(bearer()); }
    public Call<java.util.Map<String, Object>> topUp(Double amount) { return api.walletTopUp(bearer(), java.util.Map.of("amount", amount)); }
    public Call<java.util.Map<String, Object>> withdraw(Double amount) { return api.walletWithdraw(bearer(), java.util.Map.of("amount", amount)); }
}
