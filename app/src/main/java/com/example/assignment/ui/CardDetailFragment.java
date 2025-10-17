package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.assignment.R;
import com.example.assignment.data.model.Card;
import com.example.assignment.data.repository.HomeRepository;
import com.example.assignment.data.repository.TradingRepository;
import com.example.assignment.data.repository.WalletRepository;
import com.example.assignment.data.model.Transaction;
import com.example.assignment.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardDetailFragment extends Fragment {
    private static final String ARG_CARD_ID = "card_id";
    private ImageView imgCard;
    private TextView tvId, tvName, tvTeam, tvRarity, tvStatus, tvOwnerId, tvOwner, tvDesc, tvPrice, tvRejectionReason, tvImageUrl;
    private android.widget.Button btnBuy;
    private Card currentCard;
    private static final String BASE_URL = "http://10.0.2.2:8080";

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
        return inflater.inflate(R.layout.fragment_card_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgCard = view.findViewById(R.id.imgCard);
        tvId = view.findViewById(R.id.tvCardId);
        tvName = view.findViewById(R.id.tvCardName);
        tvTeam = view.findViewById(R.id.tvCardTeam);
        tvRarity = view.findViewById(R.id.tvCardRarity);
        tvStatus = view.findViewById(R.id.tvCardStatus);
        tvOwnerId = view.findViewById(R.id.tvCardOwnerId);
        tvOwner = view.findViewById(R.id.tvCardOwner);
        tvDesc = view.findViewById(R.id.tvCardDesc);
        tvPrice = view.findViewById(R.id.tvCardPrice);
        tvRejectionReason = view.findViewById(R.id.tvCardRejectionReason);
        tvImageUrl = view.findViewById(R.id.tvCardImageUrl);
        btnBuy = view.findViewById(R.id.btnBuy);

        if (getArguments() != null && getArguments().containsKey(ARG_CARD_ID)) {
            long id = getArguments().getLong(ARG_CARD_ID);
            loadCard(id);
        }
    }

    private void loadCard(long id) {
        HomeRepository repo = new HomeRepository(getContext(), "http://10.0.2.2:8080");
        repo.getHomeCard(id).enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Card c = response.body();
                    tvId.setText("ID: " + c.getId());
                    tvName.setText(c.getName());
                    tvTeam.setText("Team: " + (c.getTeam() != null ? c.getTeam() : ""));
                    tvRarity.setText("Rarity: " + (c.getRarity() != null ? c.getRarity() : ""));
                    tvStatus.setText("Status: " + (c.getStatus() != null ? c.getStatus() : ""));
                    tvOwnerId.setText("Owner ID: " + (c.getOwnerId() != null ? c.getOwnerId() : ""));
                    tvOwner.setText("Owner: " + (c.getOwnerUsername() == null ? "-" : c.getOwnerUsername()));
                    tvDesc.setText("Description: " + (c.getDescription() != null ? c.getDescription() : ""));
                    tvPrice.setText(c.getPrice() == null ? "Price: -" : ("Price: $" + c.getPrice()));
                    tvRejectionReason.setText(c.getRejectionReason() != null && !c.getRejectionReason().isEmpty() ? ("Rejection Reason: " + c.getRejectionReason()) : "");
                    tvImageUrl.setText(c.getBaseImageUrl() != null ? ("Image URL: " + c.getBaseImageUrl()) : "");
                    // image loading: set url as content description for now; you can use Glide/Picasso to load
                    imgCard.setContentDescription(c.getBaseImageUrl());
                    currentCard = c;
                    // Buy button visibility
                    SessionManager session = new SessionManager(getContext());
                    String token = session.fetchToken();
                    // show buy only for approved cards and non-owners
                    if ("APPROVED".equalsIgnoreCase(c.getStatus())) {
                        btnBuy.setVisibility(View.VISIBLE);
                        btnBuy.setEnabled(true);
                    } else {
                        btnBuy.setVisibility(View.GONE);
                    }
                    btnBuy.setOnClickListener(v -> confirmBuy());
                } else {
                    Toast.makeText(getContext(), "Failed to load card", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmBuy() {
        if (currentCard == null) return;
        // fetch wallet balance for user
        WalletRepository walletRepo = new WalletRepository(getContext(), BASE_URL);
        walletRepo.getBalance().enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                double balance = 0.0;
                if (response.isSuccessful() && response.body() != null && response.body().get("balance") != null) {
                    try { balance = Double.parseDouble(response.body().get("balance").toString()); } catch (Exception e) { }
                }
                showConfirmDialog(balance);
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
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
        TradingRepository repo = new TradingRepository(getContext(), BASE_URL);
        repo.buyCard(currentCard.getId()).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                btnBuy.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    java.util.Map<String, Object> body = response.body();
                    // update session balance if provided
                    if (body.get("newBalance") != null) {
                        try {
                            double nb = Double.parseDouble(body.get("newBalance").toString());
                            SessionManager session = new SessionManager(getContext());
                            session.saveWalletBalance(nb);
                            // also update header if available
                            android.app.Activity act = getActivity();
                            if (act != null) {
                                android.widget.TextView tvB = act.findViewById(R.id.tvBalance);
                                if (tvB != null) tvB.setText("Balance: $" + String.format(java.util.Locale.US, "%.2f", nb));
                            }
                        } catch (Exception e) { }
                    }
                    // show success
                    Toast.makeText(getContext(), "Purchase successful", Toast.LENGTH_LONG).show();
                    // remove or update UI
                    btnBuy.setVisibility(View.GONE);
                    // mark sold locally
                    currentCard.setStatus("SOLD");
                    // Navigate to InventoryFragment to view owned card
                    requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new InventoryFragment())
                        .addToBackStack(null)
                        .commit();
                } else {
                    String err = "Purchase failed";
                    try { if (response.errorBody() != null) err = response.errorBody().string(); } catch (Exception ex) { }
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                btnBuy.setEnabled(true);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
