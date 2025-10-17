package com.example.assignment.data.repository;

import android.content.Context;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.Listing;

import java.util.List;

import retrofit2.Call;

public class ListingRepository {
    private ApiService api;

    public ListingRepository(Context ctx, String baseUrl) {
        api = RetrofitClient.getClient(ctx, baseUrl).create(ApiService.class);
    }

    public ApiService getApi() { return api; }

    public Call<List<Listing>> getListings(String status) {
        return api.getListings(status);
    }
}
