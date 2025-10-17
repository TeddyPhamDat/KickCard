package com.example.assignment.data.repository;

import android.content.Context;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.HomeListing;

import java.util.List;

import retrofit2.Call;

public class HomeRepository {
    private ApiService api;

    public HomeRepository(Context ctx, String baseUrl) {
        api = RetrofitClient.getClient(ctx, baseUrl).create(ApiService.class);
    }

    public Call<List<HomeListing>> getHomeListings(String name, Double minPrice, Double maxPrice) {
        return api.getHomeListings(name, minPrice, maxPrice);
    }

    public retrofit2.Call<com.example.assignment.data.model.Card> getHomeCard(Long id) {
        return api.getHomeCard(id);
    }
}
