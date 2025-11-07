package com.example.assignment.data.api;

import com.example.assignment.data.model.Card;
import com.example.assignment.data.model.Listing;
import com.example.assignment.data.model.HomeListing;
import com.example.assignment.data.model.ListingImage;
import com.example.assignment.data.model.LoginRequest;
import com.example.assignment.data.model.RegisterRequest;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.PUT;

public interface ApiService {
    @POST("/api/auth/signin")
    Call<Map<String, String>> signin(@Body LoginRequest request);

    @POST("/api/auth/signup")
    Call<ResponseBody> signup(@Body RegisterRequest request);

    @GET("/api/cards")
    Call<List<Card>> getCards();

    @GET("/api/listings")
    Call<List<Listing>> getListings(@Query("status") String status);

    @POST("/api/listings")
    Call<Listing> createListing(@Body Listing listing);

    @GET("/api/listings/{id}/images")
    Call<List<ListingImage>> getListingImages(@Path("id") Long id);

    @PUT("/api/users/{id}")
    Call<Void> updateProfile(@Header("Authorization") String bearerToken, @Path("id") Long id, @Body com.example.assignment.data.model.UpdateProfileRequest req);
    
    @GET("/api/users/me")
    Call<com.example.assignment.data.model.User> getCurrentUser(@Header("Authorization") String bearerToken);

    // Home endpoints
    @GET("/api/home/listings")
    Call<List<HomeListing>> getHomeListings(@Query("name") String name, @Query("minPrice") Double minPrice, @Query("maxPrice") Double maxPrice);

    @GET("/api/home/cards/{id}")
    Call<com.example.assignment.data.model.Card> getHomeCard(@Path("id") Long id);

    // Wallet endpoints
    @GET("/api/wallet/balance")
    Call<java.util.Map<String, Object>> getWalletBalance(@Header("Authorization") String bearerToken);

    @POST("/api/wallet/topup")
    Call<java.util.Map<String, Object>> walletTopUp(@Header("Authorization") String bearerToken, @Body java.util.Map<String, Double> body);

    @POST("/api/wallet/withdraw")
    Call<java.util.Map<String, Object>> walletWithdraw(@Header("Authorization") String bearerToken, @Body java.util.Map<String, Double> body);

    // Trading endpoints
    @POST("/api/trading/buy/{cardId}")
    Call<java.util.Map<String, Object>> buyCard(@Header("Authorization") String bearerToken, @Path("cardId") Long cardId);

    @GET("/api/trading/transactions")
    Call<java.util.List<com.example.assignment.data.model.Transaction>> getMyTransactions(@Header("Authorization") String bearerToken);

    // My-cards endpoints (authenticated)
    @GET("/api/my-cards")
    Call<java.util.Map<String, java.util.List<com.example.assignment.data.model.MyCard>>> getMyCards(@Header("Authorization") String bearerToken);

    @GET("/api/my-cards/owned")
    Call<java.util.List<com.example.assignment.data.model.MyCard>> getOwnedCards(@Header("Authorization") String bearerToken);

    @Multipart
    @POST("/api/my-cards/with-image")
    Call<com.example.assignment.data.model.MyCard> createMyCard(
        @Header("Authorization") String bearerToken,
        @Part("name") okhttp3.RequestBody name,
        @Part("rarity") okhttp3.RequestBody rarity,
        @Part("team") okhttp3.RequestBody team,
        @Part("description") okhttp3.RequestBody description,
        @Part("price") okhttp3.RequestBody price,
        @Part okhttp3.MultipartBody.Part file
    );

    @Multipart
    @PUT("/api/my-cards/{id}/with-image")
    Call<com.example.assignment.data.model.MyCard> updateMyCard(
        @Header("Authorization") String bearerToken,
        @Path("id") Long id,
        @Part("name") okhttp3.RequestBody name,
        @Part("rarity") okhttp3.RequestBody rarity,
        @Part("team") okhttp3.RequestBody team,
        @Part("description") okhttp3.RequestBody description,
        @Part("price") okhttp3.RequestBody price,
        @Part okhttp3.MultipartBody.Part file
    );

    @DELETE("/api/my-cards/{id}")
    Call<java.util.Map<String, Boolean>> deleteMyCard(@Header("Authorization") String bearerToken, @Path("id") Long id);

    // Resell endpoints
    @POST("/api/my-cards/{id}/resell")
    Call<java.util.Map<String, Object>> resellCard(@Header("Authorization") String bearerToken, @Path("id") Long id, @Body java.util.Map<String, Object> request);

    @POST("/api/my-cards/{id}/retry-resell")
    Call<java.util.Map<String, Object>> retryResell(@Header("Authorization") String bearerToken, @Path("id") Long id, @Body java.util.Map<String, Object> request);

    @POST("/api/my-cards/{id}/cancel-resell")
    Call<java.util.Map<String, Object>> cancelResell(@Header("Authorization") String bearerToken, @Path("id") Long id);

    // Admin endpoints (cards & users)
    @GET("/api/admin/cards/pending")
    Call<java.util.List<com.example.assignment.data.model.Card>> getAdminPendingCards(@Header("Authorization") String bearerToken);

    @GET("/api/admin-cards/pending/by-owner")
    Call<java.util.List<com.example.assignment.data.model.MyCard>> getPendingCardsByOwner(@Header("Authorization") String bearerToken, @Query("ownerId") Long ownerId);

    @GET("/api/admin-cards/rejected/by-owner")
    Call<java.util.List<com.example.assignment.data.model.MyCard>> getRejectedCardsByOwner(@Header("Authorization") String bearerToken, @Query("ownerId") Long ownerId);

    @POST("/api/admin/cards/{id}/approve")
    Call<java.util.Map<String, String>> adminApproveCard(@Header("Authorization") String bearerToken, @Path("id") Long id);

    @POST("/api/admin/cards/{id}/reject")
    Call<java.util.Map<String, String>> adminRejectCard(@Header("Authorization") String bearerToken, @Path("id") Long id, @Query("reason") String reason);

    @GET("/api/admin/cards/{id}")
    Call<com.example.assignment.data.model.Card> adminGetCard(@Header("Authorization") String bearerToken, @Path("id") Long id);

    @PUT("/api/admin/cards/{id}")
    Call<com.example.assignment.data.model.Card> adminUpdateCard(@Header("Authorization") String bearerToken, @Path("id") Long id, @Body com.example.assignment.data.model.Card card);

    @DELETE("/api/admin/cards/{id}")
    Call<java.util.Map<String, String>> adminDeleteCard(@Header("Authorization") String bearerToken, @Path("id") Long id);

    @GET("/api/admin/users")
    Call<java.util.List<com.example.assignment.data.model.User>> adminListUsers(@Header("Authorization") String bearerToken);

    @PUT("/api/admin/users/{id}")
    Call<com.example.assignment.data.model.User> adminUpdateUser(@Header("Authorization") String bearerToken, @Path("id") Long id, @Body com.example.assignment.data.model.User user);

    @DELETE("/api/admin/users/{id}")
    Call<java.util.Map<String, String>> adminDeleteUser(@Header("Authorization") String bearerToken, @Path("id") Long id);

    // Payment endpoints
    @POST("/api/payments/create")
    Call<java.util.Map<String, Object>> createPayment(@Header("Authorization") String bearerToken, @Body com.example.assignment.data.model.CreatePaymentRequest request);

    @POST("/api/payments/topup")
    Call<java.util.Map<String, Object>> createWalletTopUp(@Header("Authorization") String bearerToken, @Body com.example.assignment.data.model.TopupRequest request);

    @GET("/api/payments/{orderCode}")
    Call<java.util.Map<String, Object>> getPayment(@Header("Authorization") String bearerToken, @Path("orderCode") String orderCode);

    @GET("/api/payments/my-purchases")
    Call<java.util.Map<String, Object>> getMyPurchases(@Header("Authorization") String bearerToken);

    @GET("/api/payments/wallet-transactions")
    Call<java.util.Map<String, Object>> getWalletTransactions(@Header("Authorization") String bearerToken);

    // Commented out since PayOS endpoints are disabled
    // @GET("/api/payments/my-topups")
    // Call<java.util.Map<String, Object>> getMyTopups(@Header("Authorization") String bearerToken);

    // Commented out since PayOS endpoints are disabled
    // @GET("/api/payments/my-sales")
    // Call<java.util.Map<String, Object>> getMySales(@Header("Authorization") String bearerToken);

    // @GET("/api/payments/status/{orderCode}")
    // Call<java.util.Map<String, Object>> checkPaymentStatus(@Header("Authorization") String bearerToken, @Path("orderCode") String orderCode);

    // @POST("/api/payments/force-check/{orderCode}")
    // Call<java.util.Map<String, Object>> forceCheckPayment(@Header("Authorization") String bearerToken, @Path("orderCode") String orderCode);
}
