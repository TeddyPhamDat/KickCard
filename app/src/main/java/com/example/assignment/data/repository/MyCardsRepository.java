package com.example.assignment.data.repository;

import android.content.Context;
import java.io.File;

import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.MyCard;
import com.example.assignment.data.model.Card;
import com.example.assignment.utils.SessionManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    public Call<java.util.List<MyCard>> getOwnedCards() {
        return api.getOwnedCards(bearer());
    }

    public Call<java.util.List<MyCard>> getPendingCardsByOwner(Long ownerId) {
        return api.getPendingCardsByOwner(bearer(), ownerId);
    }

    public Call<java.util.List<MyCard>> getRejectedCardsByOwner(Long ownerId) {
        return api.getRejectedCardsByOwner(bearer(), ownerId);
    }

    public Call<MyCard> createMyCard(String name, String rarity, String team, String description, Double price, File imageFile) {
        android.util.Log.d("MyCardsRepository", "Creating multipart request:");
        android.util.Log.d("MyCardsRepository", "Token: " + (bearer() != null ? "Present" : "Missing"));
        android.util.Log.d("MyCardsRepository", "Name: " + name);
        android.util.Log.d("MyCardsRepository", "Rarity: " + rarity);
        android.util.Log.d("MyCardsRepository", "Team: " + team);
        android.util.Log.d("MyCardsRepository", "Description: " + description);
        android.util.Log.d("MyCardsRepository", "Price: " + price);
        android.util.Log.d("MyCardsRepository", "Image file: " + (imageFile != null && imageFile.exists() ? imageFile.getAbsolutePath() + " (size: " + imageFile.length() + ")" : "null or not exists"));

        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name != null ? name : "");
        RequestBody rarityBody = RequestBody.create(MediaType.parse("text/plain"), rarity != null ? rarity : "");
        RequestBody teamBody = RequestBody.create(MediaType.parse("text/plain"), team != null ? team : "");
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description != null ? description : "");
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), price != null ? price.toString() : "0");

        MultipartBody.Part filePart = null;
        if (imageFile != null && imageFile.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
            filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), fileBody);
            android.util.Log.d("MyCardsRepository", "File part created: " + imageFile.getName());
        } else {
            // Create empty file part với content type image
            RequestBody emptyBody = RequestBody.create(MediaType.parse("image/jpeg"), new byte[0]);
            filePart = MultipartBody.Part.createFormData("file", "empty.jpg", emptyBody);
            android.util.Log.d("MyCardsRepository", "Empty file part created");
        }

        android.util.Log.d("MyCardsRepository", "Making API call to /api/my-cards/with-image");
        return api.createMyCard(bearer(), nameBody, rarityBody, teamBody, descriptionBody, priceBody, filePart);
    }

    public Call<MyCard> updateMyCard(Long id, String name, String rarity, String team, String description, Double price, File imageFile) {
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name != null ? name : "");
        RequestBody rarityBody = RequestBody.create(MediaType.parse("text/plain"), rarity != null ? rarity : "");
        RequestBody teamBody = RequestBody.create(MediaType.parse("text/plain"), team != null ? team : "");
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description != null ? description : "");
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), price != null ? price.toString() : "0");

        MultipartBody.Part filePart = null;
        if (imageFile != null && imageFile.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
            filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), fileBody);
        } else {
            // Create empty file part với content type image
            RequestBody emptyBody = RequestBody.create(MediaType.parse("image/jpeg"), new byte[0]);
            filePart = MultipartBody.Part.createFormData("file", "empty.jpg", emptyBody);
        }

        return api.updateMyCard(bearer(), id, nameBody, rarityBody, teamBody, descriptionBody, priceBody, filePart);
    }

    public Call<java.util.Map<String, Boolean>> deleteMyCard(Long id) {
        return api.deleteMyCard(bearer(), id);
    }

    public Call<java.util.Map<String, Object>> resellCard(Long id, Double newPrice) {
        java.util.Map<String, Object> request = new java.util.HashMap<>();
        if (newPrice != null) {
            request.put("price", newPrice);
        }
        return api.resellCard(bearer(), id, request);
    }

    public Call<java.util.Map<String, Object>> retryResell(Long id, Double newPrice) {
        java.util.Map<String, Object> request = new java.util.HashMap<>();
        if (newPrice != null) {
            request.put("price", newPrice);
        }
        return api.retryResell(bearer(), id, request);
    }

    public Call<java.util.Map<String, Object>> cancelResell(Long id) {
        return api.cancelResell(bearer(), id);
    }
}
