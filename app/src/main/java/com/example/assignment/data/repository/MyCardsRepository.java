package com.example.assignment.data.repository;

import android.content.Context;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.MyCard;
import com.example.assignment.data.model.Card;
import com.example.assignment.data.model.MyOwnedCard;
import com.example.assignment.utils.SessionManager;

import java.util.Map;

import retrofit2.Call;

public class MyCardsRepository {
    private ApiService api;
    private SessionManager session;

    public MyCardsRepository(Context ctx, String baseUrl) {
        api = RetrofitClient.getClient(ctx, baseUrl).create(ApiService.class);
        session = new SessionManager(ctx);
    }


    private String bearer() {
        String t = session.fetchToken();
        return t == null ? null : "Bearer " + t;
    }

    public Call<java.util.Map<String, java.util.List<MyCard>>> getMyCards() {
        return api.getMyCards(bearer());
    }

    public Call<java.util.List<MyOwnedCard>> getOwnedCards() {
        return api.getOwnedCards(bearer());
    }

    public Call<java.util.List<MyCard>> getPendingCardsByOwner(Long ownerId) {
        return api.getPendingCardsByOwner(bearer(), ownerId);
    }

    public Call<java.util.List<MyCard>> getRejectedCardsByOwner(Long ownerId) {
        return api.getRejectedCardsByOwner(bearer(), ownerId);
    }

    public Call<MyCard> createMyCard(MyCard card) {
        return api.createMyCard(bearer(), card);
    }

    public Call<MyCard> updateMyCard(Long id, MyCard card) {
        return api.updateMyCard(bearer(), id, card);
    }

    public Call<java.util.Map<String, Boolean>> deleteMyCard(Long id) {
        return api.deleteMyCard(bearer(), id);
    }
}
