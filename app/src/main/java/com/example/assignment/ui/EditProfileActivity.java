package com.example.assignment.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.R;
import com.example.assignment.utils.SessionManager;
import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.UpdateProfileRequest;
import com.example.assignment.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText etFullname, etPhone, etAddress;
    private MaterialButton btnSave;
    private ApiService api;
    private Long currentUserId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etFullname = findViewById(R.id.etFullname);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSaveProfile);

        api = RetrofitClient.getApiService(this);

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
                        if (u.getFullname() != null) etFullname.setText(u.getFullname());
                        if (u.getPhone() != null) etPhone.setText(u.getPhone());
                        if (u.getAddress() != null) etAddress.setText(u.getAddress());
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
                final String avatar = null;
                SessionManager session = new SessionManager(EditProfileActivity.this);
                String tok = session.fetchToken();
                if (tok != null && currentUserId != null) {
                    UpdateProfileRequest req = new UpdateProfileRequest(fullname, phone, addr, avatar);
                    Call<Void> upd = api.updateProfile("Bearer " + tok, currentUserId, req);
                    upd.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                session.saveProfile(fullname, phone, addr, avatar);
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
                } else {
                    // fallback: save locally
                    session.saveProfile(fullname, phone, addr, avatar);
                    Toast.makeText(EditProfileActivity.this, "Profile saved locally (offline)", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
