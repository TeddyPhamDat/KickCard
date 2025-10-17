package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.model.MyOwnedCard;
import com.example.assignment.data.repository.MyCardsRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.EditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.assignment.data.model.MyCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryFragment extends Fragment {
    private RecyclerView recycler;
    private InventoryAdapter adapter;
    private MyCardsRepository repo;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recyclerInventory);
        adapter = new InventoryAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
        repo = new MyCardsRepository(getContext(), BASE_URL);
        adapter.setResellListener(card -> showResellDialog(card));
        loadOwnedCards();

        // Xử lý nút Thoát ra
        View btnBack = view.findViewById(R.id.btnInventoryBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }
    private void showResellDialog(MyOwnedCard card) {
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
                        // Chuyển MyOwnedCard sang MyCard để gọi update
                        MyCard updateCard = new MyCard();
                        updateCard.setId(card.getId());
                        updateCard.setName(card.getName());
                        updateCard.setRarity(card.getRarity());
                        updateCard.setTeam(card.getTeam());
                        updateCard.setDescription(card.getDescription());
                        updateCard.setBaseImageUrl(card.getBaseImageUrl());
                        updateCard.setStatus("PENDING");
                        updateCard.setPrice(price);
                        repo.updateMyCard(card.getId(), updateCard).enqueue(new Callback<MyCard>() {
                            @Override
                            public void onResponse(Call<MyCard> call, Response<MyCard> response) {
                                if (response.isSuccessful()) loadOwnedCards();
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

    private void loadOwnedCards() {
        repo.getOwnedCards().enqueue(new Callback<List<MyOwnedCard>>() {
            @Override
            public void onResponse(Call<List<MyOwnedCard>> call, Response<List<MyOwnedCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to load inventory", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MyOwnedCard>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
