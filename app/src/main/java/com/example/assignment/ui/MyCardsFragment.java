// Path: C:/KickCard/app/src/main/java/com/example/assignment/ui/MyCardsFragment.java

package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.model.MyCard;
import com.example.assignment.data.repository.MyCardsRepository;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCardsFragment extends Fragment {
    private MaterialButtonToggleGroup toggleButtonStatus;
    private Button btnPending, btnApproved, btnRejected;
    private FloatingActionButton fabCreateListing;
    private RecyclerView recycler;
    private MyCardsAdapter adapter;
    private MyCardsRepository repo;
    private static final String BASE_URL = "http://10.0.2.2:8080";
    private Map<String, List<MyCard>> grouped;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // CHANGE 2: Initialize the toggle group
        toggleButtonStatus = view.findViewById(R.id.toggleButtonStatus);
        btnPending = view.findViewById(R.id.btnPending);
        btnApproved = view.findViewById(R.id.btnApproved);
        btnRejected = view.findViewById(R.id.btnRejected);
        recycler = view.findViewById(R.id.recyclerMyCards);
        fabCreateListing = view.findViewById(R.id.fabCreateListing);

        adapter = new MyCardsAdapter(new MyCardsAdapter.ResellListener() {
            @Override
            public void onEdit(MyCard card) { showEditDialog(card); }

            @Override
            public void onDelete(MyCard card) { confirmDelete(card); }

            @Override
            public void onResell(MyCard card) { showResellDialog(card); }

            @Override
            public void onCardClick(MyCard card) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, CardDetailFragment.newInstance(card.getId()))
                        .addToBackStack(null)
                        .commit();
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        repo = new MyCardsRepository(getContext(), BASE_URL);

        btnPending.setOnClickListener(v -> showGroup("PENDING"));
        btnApproved.setOnClickListener(v -> showGroup("APPROVED"));
        btnRejected.setOnClickListener(v -> showGroup("REJECTED"));
        fabCreateListing.setOnClickListener(v -> showCreateDialog());

        loadMyCards();
    }

    private void loadMyCards() {
        repo.getMyCards().enqueue(new Callback<Map<String, List<MyCard>>>() {
            @Override
            public void onResponse(Call<Map<String, List<MyCard>>> call, Response<Map<String, List<MyCard>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    grouped = response.body();

                    if (grouped != null) {
                        if (!grouped.containsKey("PENDING")) grouped.put("PENDING", new ArrayList<>());
                        if (!grouped.containsKey("APPROVED")) grouped.put("APPROVED", new ArrayList<>());
                        if (!grouped.containsKey("REJECTED")) grouped.put("REJECTED", new ArrayList<>());
                    } else {
                        grouped = new HashMap<>();
                        grouped.put("PENDING", new ArrayList<>());
                        grouped.put("APPROVED", new ArrayList<>());
                        grouped.put("REJECTED", new ArrayList<>());
                    }

                    // CHANGE 3: Show the "Approved" group by default
                    showGroup("APPROVED");

                    // CHANGE 4: Visually check the "Approved" button to match the data
                    if (toggleButtonStatus != null) {
                        toggleButtonStatus.check(R.id.btnApproved);
                    }

                } else {
                    Toast.makeText(getContext(), "Failed to load cards", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<MyCard>>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGroup(String status) {
        if (grouped != null && grouped.containsKey(status)) {
            adapter.setItems(grouped.get(status));
        } else {
            adapter.setItems(new ArrayList<>());
        }
    }

    // --- NO CHANGES NEEDED IN THE METHODS BELOW ---

    private void showCreateDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_my_card, null);
        EditText etName = v.findViewById(R.id.etName);
        EditText etRarity = v.findViewById(R.id.etRarity);
        EditText etImage = v.findViewById(R.id.etImageUrl);
        EditText etPrice = v.findViewById(R.id.etPrice);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Create Card Listing")
                .setView(v)
                .setPositiveButton("Create", (dialog, which) -> {
                    MyCard c = new MyCard();
                    c.setName(etName.getText().toString().trim());
                    c.setRarity(etRarity.getText().toString().trim());
                    c.setBaseImageUrl(etImage.getText().toString().trim());

                    String priceStr = etPrice.getText().toString().trim();
                    if (!priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            c.setPrice(price);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Invalid price", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    repo.createMyCard(c).enqueue(new Callback<MyCard>() {
                        @Override
                        public void onResponse(Call<MyCard> call, Response<MyCard> response) {
                            if (response.isSuccessful()) { loadMyCards(); }
                            else Toast.makeText(getContext(), "Create failed", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Call<MyCard> call, Throwable t) { Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show(); }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(MyCard card) {
        View v = getLayoutInflater().inflate(R.layout.dialog_my_card, null);
        EditText etName = v.findViewById(R.id.etName);
        EditText etRarity = v.findViewById(R.id.etRarity);
        EditText etImage = v.findViewById(R.id.etImageUrl);
        EditText etPrice = v.findViewById(R.id.etPrice);

        etName.setText(card.getName());
        etRarity.setText(card.getRarity());
        etImage.setText(card.getBaseImageUrl());

        if (card.getPrice() != null) {
            etPrice.setText(String.valueOf(card.getPrice()));
        } else {
            etPrice.setText("");
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Edit Card Listing")
                .setView(v)
                .setPositiveButton("Save", (dialog, which) -> {
                    card.setName(etName.getText().toString().trim());
                    card.setRarity(etRarity.getText().toString().trim());
                    card.setBaseImageUrl(etImage.getText().toString().trim());

                    String priceStr = etPrice.getText().toString().trim();
                    if (!priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            card.setPrice(price);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Invalid price", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        card.setPrice(null);
                    }
                    repo.updateMyCard(card.getId(), card).enqueue(new Callback<MyCard>() {
                        @Override
                        public void onResponse(Call<MyCard> call, Response<MyCard> response) {
                            if (response.isSuccessful()) {
                                loadMyCards();
                            } else Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Call<MyCard> call, Throwable t) { Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show(); }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(MyCard card) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete Card")
                .setMessage("Delete this card? This can only be done while pending.")
                .setPositiveButton("Delete", (d, w) -> {
                    repo.deleteMyCard(card.getId()).enqueue(new Callback<java.util.Map<String, Boolean>>() {
                        @Override
                        public void onResponse(Call<java.util.Map<String, Boolean>> call, Response<java.util.Map<String, Boolean>> response) {
                            if (response.isSuccessful()) loadMyCards();
                            else Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<java.util.Map<String, Boolean>> call, Throwable t) { Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show(); }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showResellDialog(MyCard card) {
        View v = getLayoutInflater().inflate(R.layout.dialog_resell_card, null);
        EditText etPrice = v.findViewById(R.id.etResellPrice);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Bán lại thẻ")
                .setView(v)
                .setPositiveButton("Bán", (dialog, which) -> {
                    String priceStr = etPrice.getText().toString().trim();
                    double price = 0;
                    try { price = Double.parseDouble(priceStr); } catch (Exception ignored) {}
                    if (price > 0) {
                        card.setPrice(price);
                        card.setStatus("APPROVED");
                        repo.updateMyCard(card.getId(), card).enqueue(new Callback<MyCard>() {
                            @Override
                            public void onResponse(Call<MyCard> call, Response<MyCard> response) {
                                if (response.isSuccessful()) loadMyCards();
                                else Toast.makeText(getContext(), "Bán lại thất bại", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(Call<MyCard> call, Throwable t) { Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show(); }
                        });
                    } else {
                        Toast.makeText(getContext(), "Giá phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}