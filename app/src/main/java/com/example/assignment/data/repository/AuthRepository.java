package com.example.assignment.data.repository;

import android.content.Context;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.LoginRequest;
import com.example.assignment.data.model.RegisterRequest;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class AuthRepository {
    private ApiService api;

    public AuthRepository(Context ctx, String baseUrl) {
        api = RetrofitClient.getClient(ctx, baseUrl).create(ApiService.class);
    }

    public Call<Map<String, String>> signin(LoginRequest req) {
        return api.signin(req);
    }

    public Call<ResponseBody> signup(RegisterRequest req) {
        return api.signup(req);
    }
}
