package com.example.assignment.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.model.MyCard;
import com.example.assignment.data.repository.MyCardsRepository;
import com.example.assignment.utils.FileUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCardsFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int STORAGE_PERMISSION_CODE = 1002;

    private MaterialButtonToggleGroup toggleButtonStatus;
    private Button btnPending, btnApproved, btnRejected, btnSold;
    private FloatingActionButton fabCreateListing;
    private RecyclerView recycler;
    private MyCardsAdapter adapter;
    private MyCardsRepository repo;
    private static final String BASE_URL = "http://10.0.2.2:8080";
    private Map<String, List<MyCard>> grouped;

    // For image selection
    private Uri selectedImageUri;
    private ImageView currentImagePreview;
    private boolean isEditMode = false;
    private MyCard currentEditCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapter();
        setupClickListeners();

        repo = new MyCardsRepository(getContext(), BASE_URL);
        loadMyCards();
    }

    private void initViews(View view) {
        toggleButtonStatus = view.findViewById(R.id.toggleButtonStatus);
        btnPending = view.findViewById(R.id.btnPending);
        btnApproved = view.findViewById(R.id.btnApproved);
        btnRejected = view.findViewById(R.id.btnRejected);
        btnSold = view.findViewById(R.id.btnSold);
        recycler = view.findViewById(R.id.recyclerMyCards);
        fabCreateListing = view.findViewById(R.id.fabCreateListing);
    }

    private void setupAdapter() {
        adapter = new MyCardsAdapter(new MyCardsAdapter.ResellListener() {
            @Override
            public void onEdit(MyCard card) {
                String status = card.getStatus() != null ? card.getStatus().toUpperCase() : "PENDING";
                if ("APPROVED".equals(status) || "SOLD".equals(status)) {
                    Toast.makeText(getContext(), "Chỉnh sửa thẻ này sẽ chuyển trạng thái về PENDING để admin duyệt lại", Toast.LENGTH_LONG).show();
                }
                showEditDialog(card);
            }

            @Override
            public void onDelete(MyCard card) { confirmDelete(card); }

            @Override
            public void onResell(MyCard card) { showResellDialog(card); }

            @Override
            public void onRetryResell(MyCard card) { showRetryResellDialog(card); }

            @Override
            public void onCancelResell(MyCard card) { confirmCancelResell(card); }

            @Override
            public void onCardClick(MyCard card) {
                // Navigate to card detail if needed
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnPending.setOnClickListener(v -> showGroup("PENDING"));
        btnApproved.setOnClickListener(v -> showGroup("APPROVED"));
        btnRejected.setOnClickListener(v -> showGroup("REJECTED"));
        btnSold.setOnClickListener(v -> showGroup("SOLD"));
        fabCreateListing.setOnClickListener(v -> showCreateDialog());
    }

    private void loadMyCards() {
        repo.getOwnedCards().enqueue(new Callback<java.util.List<MyCard>>() {
            @Override
            public void onResponse(Call<java.util.List<MyCard>> call,
                                 Response<java.util.List<MyCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Group MyCard list by status
                    java.util.List<MyCard> ownedCards = response.body();
                    grouped = new HashMap<>();
                    grouped.put("PENDING", new ArrayList<>());
                    grouped.put("APPROVED", new ArrayList<>());
                    grouped.put("REJECTED", new ArrayList<>());
                    grouped.put("SOLD", new ArrayList<>());

                    for (MyCard myCard : ownedCards) {
                        String status = myCard.getStatus() != null ? myCard.getStatus().toUpperCase() : "PENDING";
                        if (grouped.containsKey(status)) {
                            grouped.get(status).add(myCard);
                        } else {
                            grouped.get("PENDING").add(myCard); // fallback
                        }
                    }

                    // Show PENDING by default
                    showGroup("PENDING");
                    if (toggleButtonStatus != null) {
                        toggleButtonStatus.check(R.id.btnPending);
                    }

                } else {
                    Toast.makeText(getContext(), "Failed to load cards", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.List<MyCard>> call, Throwable t) {
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

    private void showCreateDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_card, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etRarity = dialogView.findViewById(R.id.etRarity);
        EditText etTeam = dialogView.findViewById(R.id.etTeam);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        ImageView imgPreview = dialogView.findViewById(R.id.imgPreview);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        selectedImageUri = null;
        currentImagePreview = imgPreview;
        isEditMode = false;

        // Reset image preview to placeholder
        imgPreview.setImageResource(R.drawable.ic_image_placeholder);
        imgPreview.setVisibility(View.VISIBLE);

        btnSelectImage.setOnClickListener(v -> {
            currentImagePreview = imgPreview; // Ensure reference is set
            selectImage();
        });

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Tạo thẻ mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String rarity = etRarity.getText().toString().trim();
                    String team = etTeam.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên thẻ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Double price = null;
                    if (!priceStr.isEmpty()) {
                        try {
                            price = Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    File imageFile = null;
                    if (selectedImageUri != null) {
                        String realPath = FileUtils.getRealPathFromURI(getContext(), selectedImageUri);
                        if (realPath != null) {
                            imageFile = new File(realPath);
                        }
                    }

                    createCard(name, rarity, team, description, price, imageFile);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditDialog(MyCard card) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_card, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etRarity = dialogView.findViewById(R.id.etRarity);
        EditText etTeam = dialogView.findViewById(R.id.etTeam);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        ImageView imgPreview = dialogView.findViewById(R.id.imgPreview);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);

        // Pre-fill data
        etName.setText(card.getName());
        etRarity.setText(card.getRarity());
        etTeam.setText(card.getTeam());
        etDescription.setText(card.getDescription());
        if (card.getPrice() != null) {
            etPrice.setText(String.valueOf(card.getPrice()));
        }

        // Load existing image
        if (card.getBaseImageUrl() != null && !card.getBaseImageUrl().isEmpty()) {
            Glide.with(this).load(card.getBaseImageUrl()).into(imgPreview);
        }

        selectedImageUri = null;
        currentImagePreview = imgPreview;
        isEditMode = true;
        currentEditCard = card;

        // Ensure ImageView is visible
        imgPreview.setVisibility(View.VISIBLE);

        btnSelectImage.setOnClickListener(v -> {
            currentImagePreview = imgPreview; // Ensure reference is set
            selectImage();
        });

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Chỉnh sửa thẻ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String rarity = etRarity.getText().toString().trim();
                    String team = etTeam.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên thẻ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Double price = null;
                    if (!priceStr.isEmpty()) {
                        try {
                            price = Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    File imageFile = null;
                    if (selectedImageUri != null) {
                        String realPath = FileUtils.getRealPathFromURI(getContext(), selectedImageUri);
                        if (realPath != null) {
                            imageFile = new File(realPath);
                        }
                    }

                    updateCard(card.getId(), name, rarity, team, description, price, imageFile);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createCard(String name, String rarity, String team, String description, Double price, File imageFile) {
        // Debug logging
        android.util.Log.d("MyCardsFragment", "Creating card with:");
        android.util.Log.d("MyCardsFragment", "Name: " + name);
        android.util.Log.d("MyCardsFragment", "Rarity: " + rarity);
        android.util.Log.d("MyCardsFragment", "Team: " + team);
        android.util.Log.d("MyCardsFragment", "Description: " + description);
        android.util.Log.d("MyCardsFragment", "Price: " + price);
        android.util.Log.d("MyCardsFragment", "Image file: " + (imageFile != null ? imageFile.getAbsolutePath() : "null"));

        repo.createMyCard(name, rarity, team, description, price, imageFile).enqueue(new Callback<MyCard>() {
            @Override
            public void onResponse(Call<MyCard> call, Response<MyCard> response) {
                android.util.Log.d("MyCardsFragment", "Response code: " + response.code());
                android.util.Log.d("MyCardsFragment", "Response message: " + response.message());

                if (response.isSuccessful()) {
                    android.util.Log.d("MyCardsFragment", "Create card successful!");
                    Toast.makeText(getContext(), "Tạo thẻ thành công! Đang chờ admin duyệt.", Toast.LENGTH_SHORT).show();
                    loadMyCards();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        android.util.Log.e("MyCardsFragment", "Create card failed: " + errorBody);
                        Toast.makeText(getContext(), "Tạo thẻ thất bại: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Tạo thẻ thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyCard> call, Throwable t) {
                android.util.Log.e("MyCardsFragment", "Network error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCard(Long id, String name, String rarity, String team, String description, Double price, File imageFile) {
        repo.updateMyCard(id, name, rarity, team, description, price, imageFile).enqueue(new Callback<MyCard>() {
            @Override
            public void onResponse(Call<MyCard> call, Response<MyCard> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thẻ thành công!", Toast.LENGTH_SHORT).show();
                    loadMyCards();
                } else {
                    Toast.makeText(getContext(), "Cập nhật thẻ thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyCard> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(MyCard card) {
        String status = card.getStatus() != null ? card.getStatus().toUpperCase() : "PENDING";
        if (!"PENDING".equals(status)) {
            Toast.makeText(getContext(), "Chỉ có thể xóa thẻ đang ở trạng thái PENDING", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Xóa thẻ")
                .setMessage("Bạn có chắc chắn muốn xóa thẻ này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    repo.deleteMyCard(card.getId()).enqueue(new Callback<java.util.Map<String, Boolean>>() {
                        @Override
                        public void onResponse(Call<java.util.Map<String, Boolean>> call, Response<java.util.Map<String, Boolean>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Xóa thẻ thành công", Toast.LENGTH_SHORT).show();
                                loadMyCards();
                            } else {
                                Toast.makeText(getContext(), "Xóa thẻ thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<java.util.Map<String, Boolean>> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void selectImage() {
        // Check for appropriate permissions based on Android version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                openImageChooser();
            }
        } else {
            // Older versions use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                openImageChooser();
            }
        }
    }

    private void openImageChooser() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Ensure there's an app that can handle this intent
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            } else {
                // Fallback to generic file picker
                Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fallbackIntent.setType("image/*");
                if (fallbackIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivityForResult(fallbackIntent, PICK_IMAGE_REQUEST);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy ứng dụng chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi mở trình chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Toast.makeText(getContext(), "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show();

            // Update preview immediately
            if (currentImagePreview != null) {
                try {
                    Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(currentImagePreview);

                    // Make sure the ImageView is visible
                    currentImagePreview.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Không thể tải ảnh preview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Lỗi: Không tìm thấy ImageView preview", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Đã hủy chọn ảnh", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Lỗi khi chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(getContext(), "Cần quyền truy cập ảnh để chọn ảnh", Toast.LENGTH_LONG).show();
                // Show settings dialog to let user enable permissions manually
                new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Cần quyền truy cập")
                    .setMessage("Ứng dụng cần quyền truy cập ảnh để bạn có thể chọn ảnh cho thẻ. Vui lòng cấp quyền trong Cài đặt.")
                    .setPositiveButton("Cài đặt", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        }
    }

    // ==================== RESELL METHODS ====================

    /**
     * Show dialog to resell a card (SOLD or APPROVED status)
     */
    private void showResellDialog(MyCard card) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_resell_card, null);
        EditText etNewPrice = dialogView.findViewById(R.id.etNewPrice);
        TextView tvCardName = dialogView.findViewById(R.id.tvCardName);
        TextView tvCurrentPrice = dialogView.findViewById(R.id.tvCurrentPrice);

        // Display card info
        tvCardName.setText(card.getName());
        if (card.getPrice() != null && card.getPrice() > 0) {
            tvCurrentPrice.setText(String.format("Current Price: $%.2f", card.getPrice()));
            etNewPrice.setText(String.format("%.2f", card.getPrice()));
        }

        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Resell Card")
            .setMessage("Set a new price to resell this card on the marketplace. It will need admin approval.")
            .setView(dialogView)
            .setPositiveButton("Resell", (dialog, which) -> {
                String priceStr = etNewPrice.getText().toString().trim();
                Double newPrice = null;
                
                if (!priceStr.isEmpty()) {
                    try {
                        newPrice = Double.parseDouble(priceStr);
                        if (newPrice <= 0) {
                            Toast.makeText(getContext(), "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                resellCard(card.getId(), newPrice);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Call API to resell card
     */
    private void resellCard(Long cardId, Double newPrice) {
        repo.resellCard(cardId, newPrice).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean success = (Boolean) response.body().get("success");
                    String message = (String) response.body().get("message");
                    
                    if (success != null && success) {
                        Toast.makeText(getContext(), message != null ? message : "Card sent for resell approval!", Toast.LENGTH_LONG).show();
                        loadMyCards(); // Reload list
                    } else {
                        Toast.makeText(getContext(), message != null ? message : "Failed to resell card", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(), "Resell failed: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Resell failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show dialog to retry resell a REJECTED card
     */
    private void showRetryResellDialog(MyCard card) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_resell_card, null);
        EditText etNewPrice = dialogView.findViewById(R.id.etNewPrice);
        TextView tvCardName = dialogView.findViewById(R.id.tvCardName);
        TextView tvCurrentPrice = dialogView.findViewById(R.id.tvCurrentPrice);
        TextView tvRejectionReason = dialogView.findViewById(R.id.tvRejectionReason);

        // Display card info
        tvCardName.setText(card.getName());
        if (card.getPrice() != null && card.getPrice() > 0) {
            tvCurrentPrice.setText(String.format("Current Price: $%.2f", card.getPrice()));
            etNewPrice.setText(String.format("%.2f", card.getPrice()));
        }

        // Display rejection reason
        if (card.getRejectionReason() != null && !card.getRejectionReason().isEmpty()) {
            tvRejectionReason.setVisibility(View.VISIBLE);
            tvRejectionReason.setText("Rejection Reason: " + card.getRejectionReason());
        } else {
            tvRejectionReason.setVisibility(View.GONE);
        }

        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Retry Resell")
            .setMessage("Update the price and resubmit for admin approval.")
            .setView(dialogView)
            .setPositiveButton("Retry", (dialog, which) -> {
                String priceStr = etNewPrice.getText().toString().trim();
                Double newPrice = null;
                
                if (!priceStr.isEmpty()) {
                    try {
                        newPrice = Double.parseDouble(priceStr);
                        if (newPrice <= 0) {
                            Toast.makeText(getContext(), "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                retryResell(card.getId(), newPrice);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Call API to retry resell
     */
    private void retryResell(Long cardId, Double newPrice) {
        repo.retryResell(cardId, newPrice).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean success = (Boolean) response.body().get("success");
                    String message = (String) response.body().get("message");
                    
                    if (success != null && success) {
                        Toast.makeText(getContext(), message != null ? message : "Card resubmitted for approval!", Toast.LENGTH_LONG).show();
                        loadMyCards(); // Reload list
                    } else {
                        Toast.makeText(getContext(), message != null ? message : "Failed to retry resell", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(), "Retry failed: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Retry failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show confirmation dialog to cancel resell
     */
    private void confirmCancelResell(MyCard card) {
        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Cancel Resell")
            .setMessage("Are you sure you want to cancel reselling \"" + card.getName() + "\"? The card will return to your collection (SOLD status).")
            .setPositiveButton("Yes, Cancel Resell", (dialog, which) -> cancelResell(card.getId()))
            .setNegativeButton("No", null)
            .show();
    }

    /**
     * Call API to cancel resell
     */
    private void cancelResell(Long cardId) {
        repo.cancelResell(cardId).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean success = (Boolean) response.body().get("success");
                    String message = (String) response.body().get("message");
                    
                    if (success != null && success) {
                        Toast.makeText(getContext(), message != null ? message : "Resell cancelled. Card returned to your collection.", Toast.LENGTH_LONG).show();
                        loadMyCards(); // Reload list
                    } else {
                        Toast.makeText(getContext(), message != null ? message : "Failed to cancel resell", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(), "Cancel failed: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Cancel failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
