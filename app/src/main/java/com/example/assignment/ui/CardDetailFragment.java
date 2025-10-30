package com.example.assignment.ui;

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
            btnBuy.setOnClickListener(v -> confirmBuy());
        } else {
            btnBuy.setVisibility(View.GONE);
        }
    }

    // --- YOUR EXISTING BUYING LOGIC: NO CHANGES NEEDED ---

    private void confirmBuy() {
        if (currentCard == null) return;
        // fetch wallet balance for user
        WalletRepository walletRepo = new WalletRepository(getContext(), BASE_URL);
        walletRepo.getBalance().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                double balance = 0.0;
                if (response.isSuccessful() && response.body() != null && response.body().get("balance") != null) {
                    try {
                        balance = Double.parseDouble(response.body().get("balance").toString());
                    } catch (Exception e) { /* ignore */ }
                }
                showConfirmDialog(balance);
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showConfirmDialog(0.0);
            }
        });
    }

    private void showConfirmDialog(double balance) {
        String msg = "Buy " + currentCard.getName() + " for $" + currentCard.getPrice() + "?\nYour balance: $" + balance;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                .setTitle("Confirm Purchase")
                .setMessage(msg)
                .setPositiveButton("Buy", (d, w) -> doBuy())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doBuy() {
        if (currentCard == null) return;
        btnBuy.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        TradingRepository repo = new TradingRepository(getContext(), BASE_URL);
        repo.buyCard(currentCard.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnBuy.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    // update session balance if provided
                    if (body.get("newBalance") != null) {
                        try {
                            double nb = Double.parseDouble(body.get("newBalance").toString());
                            SessionManager session = new SessionManager(getContext());
                            session.saveWalletBalance(nb);
                        } catch (Exception e) { /* ignore */ }
                    }
                    // show success
                    Toast.makeText(getContext(), "Purchase successful", Toast.LENGTH_LONG).show();
                    // remove or update UI
                    btnBuy.setVisibility(View.GONE);
                    // mark sold locally
                    currentCard.setStatus("SOLD");
                    // Optionally, navigate away or refresh
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    String err = "Purchase failed";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ex) { /* ignore */ }
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnBuy.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}