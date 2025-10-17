package com.example.assignment.data.repository;

import android.content.Context;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.utils.SessionManager;

import retrofit2.Call;

public class AdminRepository {
    private ApiService api;
    private SessionManager session;

    public AdminRepository(Context ctx, String baseUrl) {
        api = RetrofitClient.getClient(ctx, baseUrl).create(ApiService.class);
        session = new SessionManager(ctx);
    }

    private String bearer() {
        String t = session.fetchToken();
        return t == null ? null : "Bearer " + t;
    }

    public Call<java.util.List<com.example.assignment.data.model.Card>> getPendingCards() { return api.getAdminPendingCards(bearer()); }
    public Call<java.util.Map<String, String>> approveCard(Long id) { return api.adminApproveCard(bearer(), id); }
    public Call<java.util.Map<String, String>> rejectCard(Long id, String reason) { return api.adminRejectCard(bearer(), id, reason); }
    public Call<com.example.assignment.data.model.Card> getCard(Long id) { return api.adminGetCard(bearer(), id); }
    public Call<com.example.assignment.data.model.Card> updateCard(Long id, com.example.assignment.data.model.Card card) { return api.adminUpdateCard(bearer(), id, card); }
    public Call<java.util.Map<String, String>> deleteCard(Long id) { return api.adminDeleteCard(bearer(), id); }

    public Call<java.util.List<com.example.assignment.data.model.User>> listUsers() { return api.adminListUsers(bearer()); }
    public Call<com.example.assignment.data.model.User> updateUser(Long id, com.example.assignment.data.model.User user) { return api.adminUpdateUser(bearer(), id, user); }
    public Call<java.util.Map<String, String>> deleteUser(Long id) { return api.adminDeleteUser(bearer(), id); }
}
