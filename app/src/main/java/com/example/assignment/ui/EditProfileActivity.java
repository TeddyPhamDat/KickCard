package com.example.assignment.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.utils.SessionManager;
import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.UpdateProfileRequest;
import com.example.assignment.data.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1002;
    
    private TextInputEditText etFullname, etPhone, etAddress, etAvatarUrl;
    private MaterialButton btnSave, btnChooseAvatar, btnEnterAvatarUrl;
    private ImageView ivProfileAvatar;
    private com.google.android.material.textfield.TextInputLayout tilAvatarUrl;
    private ApiService api;
    private Long currentUserId = null;
    private Uri selectedImageUri = null;
    private String currentAvatarUrl = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Toast.makeText(this, "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show();
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(ivProfileAvatar);
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "Đã hủy chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etFullname = findViewById(R.id.etFullname);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        tilAvatarUrl = findViewById(R.id.tilAvatarUrl);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnChooseAvatar = findViewById(R.id.btnChooseAvatar);
        btnEnterAvatarUrl = findViewById(R.id.btnEnterAvatarUrl);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);

        api = RetrofitClient.getApiService(this);

        btnChooseAvatar.setOnClickListener(v -> {
            openImagePicker();
        });
        
        btnEnterAvatarUrl.setOnClickListener(v -> {
            if (tilAvatarUrl.getVisibility() == View.VISIBLE) {
                tilAvatarUrl.setVisibility(View.GONE);
            } else {
                tilAvatarUrl.setVisibility(View.VISIBLE);
            }
        });

        // prefill from local storage if any
        SessionManager sess = new SessionManager(this);
        etFullname.setText(sess.getFullname());
        etPhone.setText(sess.getPhone());
        etAddress.setText(sess.getAddress());

        // fetch current user from backend to get id and latest data
        String token = sess.fetchToken();
        if (token != null) {
            Call<User> call = api.getCurrentUser("Bearer " + token);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User u = response.body();
                        currentUserId = u.getId();
                        currentAvatarUrl = u.getAvatarUrl();
                        if (u.getFullname() != null) etFullname.setText(u.getFullname());
                        if (u.getPhone() != null) etPhone.setText(u.getPhone());
                        if (u.getAddress() != null) etAddress.setText(u.getAddress());
                        
                        // Load avatar
                        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            String imageUrl = currentAvatarUrl;
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "http://10.0.2.2:8080" + (imageUrl.startsWith("/") ? "" : "/") + imageUrl;
                            }
                            etAvatarUrl.setText(currentAvatarUrl);
                            Glide.with(EditProfileActivity.this)
                                    .load(imageUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(ivProfileAvatar);
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // ignore; keep local values
                }
            });
        }

        // prefill from local storage if any (for now, session only stores token)
        // you can extend SessionManager to store profile fields

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullname = etFullname.getText().toString().trim();
                final String phone = etPhone.getText().toString().trim();
                final String addr = etAddress.getText().toString().trim();
                final String avatarUrl = etAvatarUrl.getText().toString().trim();
                
                SessionManager session = new SessionManager(EditProfileActivity.this);
                String tok = session.fetchToken();
                
                if (tok != null && currentUserId != null) {
                    // If user selected an image, upload it first
                    if (selectedImageUri != null) {
                        uploadAvatarAndSaveProfile(tok, currentUserId, fullname, phone, addr);
                    } else {
                        // Use URL from input or keep current
                        String finalAvatarUrl = avatarUrl.isEmpty() ? currentAvatarUrl : avatarUrl;
                        saveProfile(tok, currentUserId, fullname, phone, addr, finalAvatarUrl);
                    }
                } else {
                    // fallback: save locally
                    String finalAvatarUrl = avatarUrl.isEmpty() ? currentAvatarUrl : avatarUrl;
                    session.saveProfile(fullname, phone, addr, finalAvatarUrl);
                    Toast.makeText(EditProfileActivity.this, "Profile saved locally (offline)", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void openImagePicker() {
        // Check for appropriate permissions based on Android version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                launchImagePicker();
            }
        } else {
            // Older versions use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                launchImagePicker();
            }
        }
    }

    private void launchImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            if (intent.resolveActivity(getPackageManager()) != null) {
                imagePickerLauncher.launch(intent);
            } else {
                Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fallbackIntent.setType("image/*");
                if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
                    imagePickerLauncher.launch(fallbackIntent);
                } else {
                    Toast.makeText(this, "Không tìm thấy ứng dụng chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi mở trình chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                Toast.makeText(this, "Cần quyền truy cập ảnh để chọn ảnh", Toast.LENGTH_LONG).show();
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Cần quyền truy cập")
                    .setMessage("Ứng dụng cần quyền truy cập ảnh để bạn có thể chọn avatar. Vui lòng cấp quyền trong Cài đặt.")
                    .setPositiveButton("Cài đặt", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        }
    }

    private void uploadAvatarAndSaveProfile(String token, Long userId, String fullname, String phone, String address) {
        try {
            File file = createFileFromUri(selectedImageUri);
            if (file == null) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            api.uploadAvatar("Bearer " + token, userId, body).enqueue(new Callback<java.util.Map<String, String>>() {
                @Override
                public void onResponse(Call<java.util.Map<String, String>> call, Response<java.util.Map<String, String>> response) {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        String avatarUrl = response.body().get("url");
                        
                        // Update UI immediately with new avatar
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) {
                                Glide.with(EditProfileActivity.this)
                                        .load(avatarUrl)
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_person)
                                        .error(R.drawable.ic_person)
                                        .into(ivProfileAvatar);
                            }
                        });
                        
                        // Save profile with new avatar URL
                        saveProfile(token, userId, fullname, phone, address, avatarUrl);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Avatar upload failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                    Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile(String token, Long userId, String fullname, String phone, String address, String avatarUrl) {
        UpdateProfileRequest req = new UpdateProfileRequest(fullname, phone, address, avatarUrl);
        api.updateProfile("Bearer " + token, userId, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    SessionManager session = new SessionManager(EditProfileActivity.this);
                    session.saveProfile(fullname, phone, address, avatarUrl);
                    Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File file = new File(getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
