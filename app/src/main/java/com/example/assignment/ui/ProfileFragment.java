package com.example.assignment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import com.example.assignment.R;
import com.example.assignment.auth.login.LoginActivity;
import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.User;
import com.example.assignment.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView tvName, tvEmail, tvPhone, tvAddress;
    private TextView tvBalance;
    private Button btnEdit, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvName = view.findViewById(R.id.tvProfName);
        tvEmail = view.findViewById(R.id.tvProfEmail);
        tvPhone = view.findViewById(R.id.tvProfPhone);
        tvAddress = view.findViewById(R.id.tvProfAddress);
    tvBalance = view.findViewById(R.id.tvProfBalance);
        btnEdit = view.findViewById(R.id.btnProfEdit);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadProfileData();
        btnEdit.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            SessionManager session = new SessionManager(getContext());
            session.clear(); // Clear JWT token and profile data
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData(); // Refresh when returning from edit
    }

    private void loadProfileData() {
        SessionManager session = new SessionManager(getContext());
        String token = session.fetchToken();
        
        if (token != null) {
            Log.d("ProfileFragment", "loadProfileData: token present, length=" + token.length());
            ApiService api = RetrofitClient.getApiService(getContext());
            api.getCurrentUser("Bearer " + token).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User u = response.body();
                        updateUI(u);
                        // fetch wallet balance
                        com.example.assignment.data.api.ApiService api2 = com.example.assignment.data.api.RetrofitClient.getApiService(getContext());
                        api2.getWalletBalance("Bearer " + token).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                            @Override
                            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().get("balance") != null) {
                                    try {
                                        double bal = Double.parseDouble(response.body().get("balance").toString());
                                        tvBalance.setText("Balance: $" + String.format(java.util.Locale.US, "%.2f", bal));
                                        session.saveWalletBalance(bal);
                                    } catch (Exception e) { }
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) { }
                        });
                        // Save to session for offline access
                        session.saveProfile(u.getFullname(), u.getPhone(), u.getAddress(), u.getAvatarUrl());
                    } else {
                        // Log details for debugging
                        try {
                            String err = response.errorBody() != null ? response.errorBody().string() : "<no body>";
                            Log.w("ProfileFragment", "getCurrentUser failed: code=" + response.code() + " body=" + err);
                            if (getContext() != null) {
                                android.widget.Toast.makeText(getContext(), "Profile load failed: " + response.code(), android.widget.Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            Log.e("ProfileFragment", "error reading errorBody", ex);
                        }
                        loadFromSession(session);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    loadFromSession(session);
                }
            });
        } else {
            loadFromSession(session);
        }
    }

    private void updateUI(User user) {
        String displayName = user.getFullname();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = user.getUsername();
        }
        tvName.setText(displayName);
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "No phone");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "No address");
        // show cached balance if available
        SessionManager s = new SessionManager(getContext());
        Double bal = s.getWalletBalance();
        if (bal != null) tvBalance.setText("Balance: $" + String.format(java.util.Locale.US, "%.2f", bal));
    }

    private void loadFromSession(SessionManager session) {
        // Fallback to session data if API fails
        tvName.setText(session.getFullname() != null ? session.getFullname() : "User");
        tvEmail.setText("Email not available");
        tvPhone.setText(session.getPhone() != null ? session.getPhone() : "No phone");
        tvAddress.setText(session.getAddress() != null ? session.getAddress() : "No address");
        Double bal = session.getWalletBalance();
        if (bal != null) tvBalance.setText("Balance: $" + String.format(java.util.Locale.US, "%.2f", bal));
    }
}
