package com.example.assignment.data.repository;

import android.content.Context;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.utils.SessionManager;

import retrofit2.Call;

public class TradingRepository {
    private ApiService api;
    private SessionManager session;

    public TradingRepository(Context ctx, String baseUrl) {
        api = RetrofitClient.getClient(ctx, baseUrl).create(ApiService.class);
        session = new SessionManager(ctx);
    }

    private String bearer() {
        String t = session.fetchToken();
        return t == null ? null : "Bearer " + t;
    }

    public Call<java.util.Map<String, Object>> buyCard(Long cardId) { return api.buyCard(bearer(), cardId); }
    public Call<java.util.List<com.example.assignment.data.model.Transaction>> getMyTransactions() { return api.getMyTransactions(bearer()); }
}
