package com.example.assignment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.assignment.R;
import com.example.assignment.data.model.MyCard;
import com.example.assignment.data.repository.MyCardsRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCardsFragment extends Fragment {
    private Button btnPending, btnApproved, btnRejected, btnCreate;
    private Button btnInventory;
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
        btnPending = view.findViewById(R.id.btnPending);
        btnApproved = view.findViewById(R.id.btnApproved);
        btnRejected = view.findViewById(R.id.btnRejected);
        btnCreate = view.findViewById(R.id.btnCreateListing);
        recycler = view.findViewById(R.id.recyclerMyCards);

        btnInventory = view.findViewById(R.id.btnInventory);

        adapter = new MyCardsAdapter(new MyCardsAdapter.ResellListener() {
            @Override
            public void onEdit(MyCard card) { showEditDialog(card); }

            @Override
            public void onDelete(MyCard card) { confirmDelete(card); }

            @Override
            public void onResell(MyCard card) { showResellDialog(card); }

            @Override
            public void onCardClick(MyCard card) {
                // Mở CardDetailFragment khi click vào item
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, CardDetailFragment.newInstance(card.getId()))
                    .addToBackStack(null)
                    .commit();
            }
        });
    // ...existing code...
    // Di chuyển showResellDialog xuống cuối class
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        repo = new MyCardsRepository(getContext(), BASE_URL);

        btnPending.setOnClickListener(v -> showGroup("PENDING"));
        btnApproved.setOnClickListener(v -> showGroup("APPROVED"));
        btnRejected.setOnClickListener(v -> showGroup("REJECTED"));
        btnCreate.setOnClickListener(v -> showCreateDialog());

        btnInventory.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new InventoryFragment())
                .addToBackStack(null)
                .commit();
        });

        loadMyCards();
    }

    private void loadMyCards() {
        // Khởi tạo grouped với các key rỗng
        grouped = new java.util.HashMap<>();
        grouped.put("PENDING", new ArrayList<>());
        grouped.put("APPROVED", new ArrayList<>());
        grouped.put("REJECTED", new ArrayList<>());
        
        com.example.assignment.utils.SessionManager sessionManager = new com.example.assignment.utils.SessionManager(getContext());
        Long userId = sessionManager.fetchUserId();
        
        if (userId == -1L) {
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load các thẻ approved/sold từ API cũ
        repo.getMyCards().enqueue(new Callback<Map<String, List<MyCard>>>() {
            @Override
            public void onResponse(Call<Map<String, List<MyCard>>> call, Response<Map<String, List<MyCard>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, List<MyCard>> oldData = response.body();
                    if (oldData.containsKey("APPROVED")) {
                        grouped.put("APPROVED", oldData.get("APPROVED"));
                    }
                    if (oldData.containsKey("SOLD")) {
                        grouped.put("SOLD", oldData.get("SOLD"));
                    }
                    
                    // Load pending cards từ API mới
                    loadPendingCards(userId);
                } else {
                    loadPendingCards(userId);
                }
            }
            @Override
            public void onFailure(Call<Map<String, List<MyCard>>> call, Throwable t) {
                loadPendingCards(userId);
            }
        });
    }

    private void loadPendingCards(Long userId) {
        repo.getPendingCardsByOwner(userId).enqueue(new Callback<List<MyCard>>() {
            @Override
            public void onResponse(Call<List<MyCard>> call, Response<List<MyCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Debug: Log giá của thẻ PENDING
                    for (MyCard card : response.body()) {
                        android.util.Log.d("MyCardsFragment", "PENDING card " + card.getName() + " price: " + card.getPrice());
                    }
                    grouped.put("PENDING", response.body());
                }
                loadRejectedCards(userId);
            }
            @Override
            public void onFailure(Call<List<MyCard>> call, Throwable t) {
                loadRejectedCards(userId);
            }
        });
    }

    private void loadRejectedCards(Long userId) {
        repo.getRejectedCardsByOwner(userId).enqueue(new Callback<List<MyCard>>() {
            @Override
            public void onResponse(Call<List<MyCard>> call, Response<List<MyCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    grouped.put("REJECTED", response.body());
                }
                
                // Debug log sau khi load xong tất cả
                android.util.Log.d("MyCardsFragment", "Final data:");
                for (String key : grouped.keySet()) {
                    android.util.Log.d("MyCardsFragment", "Key: " + key + ", Size: " + grouped.get(key).size());
                }
                
                // Hiển thị tab đầu tiên có dữ liệu
                if (!grouped.get("PENDING").isEmpty()) {
                    showGroup("PENDING");
                } else if (!grouped.get("APPROVED").isEmpty()) {
                    showGroup("APPROVED");
                } else if (!grouped.get("SOLD").isEmpty()) {
                    showGroup("SOLD");
                } else {
                    showGroup("PENDING");
                }
            }
            @Override
            public void onFailure(Call<List<MyCard>> call, Throwable t) {
                // Hiển thị tab mặc định ngay cả khi có lỗi
                showGroup("PENDING");
            }
        });
    }

    private void showGroup(String status) {
        if (grouped == null) return;
        List<MyCard> list = grouped.get(status.toUpperCase());
        if (list == null) list = new ArrayList<>();
        
        // Debug: Log số lượng thẻ cho từng trạng thái
        android.util.Log.d("MyCardsFragment", "Status: " + status + ", Count: " + list.size());
        
        adapter.setItems(list);
        
        // Hiển thị thông báo nếu không có thẻ nào
        if (list.isEmpty()) {
            String message = "Không có thẻ nào ở trạng thái " + status;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreateDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_my_card, null);
        EditText etName = v.findViewById(R.id.etName);
        EditText etRarity = v.findViewById(R.id.etRarity);
        EditText etTeam = v.findViewById(R.id.etTeam);
        EditText etDesc = v.findViewById(R.id.etDescription);
        EditText etImage = v.findViewById(R.id.etImageUrl);
        EditText etPrice = v.findViewById(R.id.etPrice);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Create Card")
                .setView(v)
                .setPositiveButton("Create", (dialog, which) -> {
                    MyCard c = new MyCard();
                    c.setName(etName.getText().toString().trim());
                    c.setRarity(etRarity.getText().toString().trim());
                    c.setTeam(etTeam.getText().toString().trim());
                    c.setDescription(etDesc.getText().toString().trim());
                    c.setBaseImageUrl(etImage.getText().toString().trim());
                    
                    // Thêm giá tiền nếu có nhập
                    String priceStr = etPrice.getText().toString().trim();
                    if (!priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            c.setPrice(price);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Giá tiền không hợp lệ", Toast.LENGTH_SHORT).show();
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
        EditText etTeam = v.findViewById(R.id.etTeam);
        EditText etDesc = v.findViewById(R.id.etDescription);
        EditText etImage = v.findViewById(R.id.etImageUrl);
        EditText etPrice = v.findViewById(R.id.etPrice);

        etName.setText(card.getName());
        etRarity.setText(card.getRarity());
        etTeam.setText(card.getTeam());
        etDesc.setText(card.getDescription());
        etImage.setText(card.getBaseImageUrl());
        
        // Debug log để kiểm tra giá tiền
        android.util.Log.d("MyCardsFragment", "Edit card - Price: " + card.getPrice());
        
        if (card.getPrice() != null) {
            etPrice.setText(String.valueOf(card.getPrice()));
        } else {
            etPrice.setText(""); // Đảm bảo clear nếu không có giá
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Edit Card")
                .setView(v)
                .setPositiveButton("Save", (dialog, which) -> {
                    card.setName(etName.getText().toString().trim());
                    card.setRarity(etRarity.getText().toString().trim());
                    card.setTeam(etTeam.getText().toString().trim());
                    card.setDescription(etDesc.getText().toString().trim());
                    card.setBaseImageUrl(etImage.getText().toString().trim());
                    
                    // Cập nhật giá tiền
                    String priceStr = etPrice.getText().toString().trim();
                    if (!priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            card.setPrice(price);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Giá tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        card.setPrice(null);
                    }
                    repo.updateMyCard(card.getId(), card).enqueue(new Callback<MyCard>() {
                        @Override
                        public void onResponse(Call<MyCard> call, Response<MyCard> response) {
                            if (response.isSuccessful()) {
                                // Debug: Log response để xem có trả về price không
                                MyCard updatedCard = response.body();
                                if (updatedCard != null) {
                                    android.util.Log.d("MyCardsFragment", "Updated card price: " + updatedCard.getPrice());
                                }
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
