package com.example.assignment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.model.Card;
import com.example.assignment.data.repository.HomeRepository;
import com.example.assignment.data.repository.TradingRepository;
import com.example.assignment.data.repository.WalletRepository;
import com.example.assignment.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardDetailFragment extends Fragment {
    private static final String ARG_CARD_ID = "card_id";
    private static final String BASE_URL = "http://10.0.2.2:8080";

    // Views from the new modern layout
    private ImageView imgCard;
    private TextView tvCardName, tvCardRarity, tvCardStatus, tvCardOwner, tvCardPrice, tvCardRejectionReason;
    private Button btnBuy;
    private MaterialCardView detailsCard, rejectionCard;
    private View progressBar;

    private Card currentCard;

    public static CardDetailFragment newInstance(Long cardId) {
        CardDetailFragment f = new CardDetailFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_CARD_ID, cardId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the new, modern layout
        return inflater.inflate(R.layout.fragment_card_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all the views from the new layout using their correct IDs
        imgCard = view.findViewById(R.id.imgCard);
        tvCardName = view.findViewById(R.id.tvCardName);
        tvCardRarity = view.findViewById(R.id.tvCardRarity);
        tvCardStatus = view.findViewById(R.id.tvCardStatus);
        tvCardOwner = view.findViewById(R.id.tvCardOwner);
        tvCardPrice = view.findViewById(R.id.tvCardPrice);
        tvCardRejectionReason = view.findViewById(R.id.tvCardRejectionReason);
        btnBuy = view.findViewById(R.id.btnBuy);
        detailsCard = view.findViewById(R.id.detailsCard);
        rejectionCard = view.findViewById(R.id.rejectionCard);
        progressBar = view.findViewById(R.id.progressBar);

        if (getArguments() != null && getArguments().containsKey(ARG_CARD_ID)) {
            long id = getArguments().getLong(ARG_CARD_ID);
            loadCard(id);
        }
    }

    private void loadCard(long id) {
        // Uses your existing HomeRepository
        HomeRepository repo = new HomeRepository(getContext(), BASE_URL);
        progressBar.setVisibility(View.VISIBLE);
        btnBuy.setEnabled(false);

        repo.getHomeCard(id).enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentCard = response.body();
                    bindData(currentCard); // Populate the UI with the fetched data
                } else {
                    Toast.makeText(getContext(), "Failed to load card", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindData(Card card) {
        // Use Glide (which is in your project) to load the image
        Glide.with(this)
                .load(card.getBaseImageUrl())
                .placeholder(R.drawable.ic_soccer_ball)
                .into(imgCard);

        // Set the text for the main info
        tvCardName.setText(card.getName());
        tvCardRarity.setText(card.getRarity());

        // Set text for the details card
        tvCardStatus.setText(card.getStatus() != null ? card.getStatus().toUpperCase() : "N/A");
        tvCardOwner.setText(card.getOwnerUsername() != null ? card.getOwnerUsername() : "Unowned");

        // Handle price and visibility of the details card
        if (card.getPrice() != null && card.getPrice() > 0) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvCardPrice.setText(format.format(card.getPrice()));
            detailsCard.setVisibility(View.VISIBLE);
        } else {
            detailsCard.setVisibility(View.GONE);
        }

        // Handle rejection reason and visibility of the rejection card
        if ("REJECTED".equalsIgnoreCase(card.getStatus()) && card.getRejectionReason() != null && !card.getRejectionReason().isEmpty()) {
            rejectionCard.setVisibility(View.VISIBLE);
            tvCardRejectionReason.setText(card.getRejectionReason());
        } else {
            rejectionCard.setVisibility(View.GONE);
        }

        // Handle the visibility of the "Buy" button
        SessionManager sessionManager = new SessionManager(getContext());
        Long currentUserId = sessionManager.fetchUserId();
        Long cardOwnerId = card.getOwnerId();

        boolean isOwnedByCurrentUser = false; // Default to false
        // This check is now safe. It only compares if BOTH IDs are valid.
        if (currentUserId != null && currentUserId != -1L && cardOwnerId != null) {
            isOwnedByCurrentUser = cardOwnerId.equals(currentUserId);
        }

        if ("APPROVED".equalsIgnoreCase(card.getStatus()) && !isOwnedByCurrentUser) {
            btnBuy.setVisibility(View.VISIBLE);
            btnBuy.setEnabled(true);
            btnBuy.setOnClickListener(v -> openPaymentActivity());
        } else {
            btnBuy.setVisibility(View.GONE);
        }
    }

    // --- NEW PURCHASE LOGIC USING TRADING API ---

    private void openPaymentActivity() {
        if (currentCard == null) return;

        // Show confirmation dialog
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận mua thẻ")
            .setMessage(String.format("Bạn có chắc chắn muốn mua thẻ '%s' với giá %,.0f VND?\n\nSố dư ví của bạn sẽ bị trừ.",
                currentCard.getName(),
                currentCard.getPrice()))
            .setPositiveButton("Mua", (dialog, which) -> purchaseCard())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void purchaseCard() {
        if (currentCard == null) return;

        // Show loading
        btnBuy.setEnabled(false);
        btnBuy.setText("Đang xử lý...");

        // Get token using SessionManager
        com.example.assignment.utils.SessionManager sessionManager = 
            new com.example.assignment.utils.SessionManager(requireContext());
        String token = sessionManager.fetchToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            btnBuy.setEnabled(true);
            btnBuy.setText("Mua ngay");
            return;
        }

        // Call API to buy card
        com.example.assignment.data.api.ApiService apiService = 
            com.example.assignment.data.api.RetrofitClient.getClient(requireContext(), "http://10.0.2.2:8080")
                .create(com.example.assignment.data.api.ApiService.class);

        apiService.buyCard("Bearer " + token, currentCard.getId()).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");
                    
                    if (success != null && success) {
                        // Purchase successful
                        Toast.makeText(getContext(), "🎉 Mua thẻ thành công!", Toast.LENGTH_LONG).show();
                        btnBuy.setVisibility(View.GONE);
                        
                        // Refresh wallet balance
                        refreshWalletBalance();
                        
                        // Navigate back or refresh
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        String error = (String) result.get("error");
                        Toast.makeText(getContext(), "Lỗi: " + (error != null ? error : "Không xác định"), Toast.LENGTH_LONG).show();
                        btnBuy.setEnabled(true);
                        btnBuy.setText("Mua ngay");
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    btnBuy.setEnabled(true);
                    btnBuy.setText("Mua ngay");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnBuy.setEnabled(true);
                btnBuy.setText("Mua ngay");
            }
        });
    }

    private void refreshWalletBalance() {
        // Get SessionManager for later use
        final com.example.assignment.utils.SessionManager sessionManager = 
            new com.example.assignment.utils.SessionManager(requireContext());
        String token = sessionManager.fetchToken();

        if (token == null || token.isEmpty()) return;

        // Use direct HTTP call instead of ApiService to handle dynamic response
        final String finalToken = token;
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("http://10.0.2.2:8080/api/users/me");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + finalToken);
                conn.setRequestProperty("Content-Type", "application/json");
                
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    // Parse JSON response
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                    if (jsonResponse.getBoolean("success")) {
                        org.json.JSONObject userData = jsonResponse.getJSONObject("data");
                        double newBalance = userData.getDouble("walletBalance");
                        
                        // Update using SessionManager
                        sessionManager.saveWalletBalance(newBalance);
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == getActivity().RESULT_OK) {
            // Payment thành công, ẩn nút buy và refresh card status
            btnBuy.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Giao dịch hoàn tất!", Toast.LENGTH_LONG).show();

            // Optionally refresh card data hoặc navigate back
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
}