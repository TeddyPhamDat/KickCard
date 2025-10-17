package com.example.assignment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.assignment.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.User;
import com.example.assignment.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        bottomNav = findViewById(R.id.bottomNav);

        // default fragment -> Home screen
        replaceFragment(new HomeFragment());
        bottomNav.setSelectedItemId(R.id.menu_home);
        
        // Initially hide admin tab until we check user role
        bottomNav.getMenu().findItem(R.id.menu_admin).setVisible(false);    bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment f = null;
                if (id == R.id.menu_home) f = new HomeFragment();
                else if (id == R.id.menu_my_cards) f = new MyCardsFragment();
                else if (id == R.id.menu_wallet) f = new WalletFragment();
                else if (id == R.id.menu_profile) f = new ProfileFragment();
                else if (id == R.id.menu_admin) f = new AdminFragment();
                if (f != null) replaceFragment(f);
                return true;
            }
        });

        // Header profile name opens ProfileFragment for viewing profile details
        findViewById(R.id.tvProfileName).setOnClickListener(v -> replaceFragment(new ProfileFragment()));
        
        // FAB for quick edit access from any screen
        findViewById(R.id.fabEditProfile).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EditProfileActivity.class)));
    }

    private void replaceFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, f).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileHeader();
    }

    private void refreshProfileHeader() {
        SessionManager session = new SessionManager(this);
        String token = session.fetchToken();
        android.util.Log.d("MainActivity", "refreshProfileHeader: token=" + (token == null ? "<null>" : (token.length() + " chars")));
        android.widget.TextView tvProfile = findViewById(R.id.tvProfileName);
        
        if (token != null && !token.isEmpty()) {
            ApiService api = RetrofitClient.getApiService(this);
            Call<User> call = api.getCurrentUser("Bearer " + token);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User u = response.body();
                        String displayName = u.getFullname();
                        if (displayName == null || displayName.trim().isEmpty()) {
                            displayName = u.getUsername();
                        }
                        tvProfile.setText("Hello, " + displayName);
                        session.saveProfile(u.getFullname(), u.getPhone(), u.getAddress(), u.getAvatarUrl());
                        session.saveUserId(u.getId());
                        session.saveUsername(u.getUsername());
                        
                        // Show/hide admin tab based on role
                        boolean isAdmin = u.getRole() != null && u.getRole().toUpperCase().contains("ADMIN");
                        bottomNav.getMenu().findItem(R.id.menu_admin).setVisible(isAdmin);
                        // Admins shouldn't see My Cards or Wallet tabs
                        bottomNav.getMenu().findItem(R.id.menu_my_cards).setVisible(!isAdmin);
                        bottomNav.getMenu().findItem(R.id.menu_wallet).setVisible(!isAdmin);
                        
                        // if user is an admin, open admin fragment by default
                        if (isAdmin) {
                            replaceFragment(new com.example.assignment.ui.AdminFragment());
                            bottomNav.setSelectedItemId(R.id.menu_admin);
                        }
                        // fetch wallet balance and update header
                        com.example.assignment.data.api.ApiService api2 = com.example.assignment.data.api.RetrofitClient.getApiService(MainActivity.this);
                        api2.getWalletBalance("Bearer " + token).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                            @Override
                            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Object b = response.body().get("balance");
                                    try {
                                        double bal = Double.parseDouble(b.toString());
                                        SessionManager s2 = new SessionManager(MainActivity.this);
                                        s2.saveWalletBalance(bal);
                                        android.widget.TextView tvB = findViewById(R.id.tvBalance);
                                        tvB.setText("Balance: $" + String.format(java.util.Locale.US, "%.2f", bal));
                                    } catch (Exception e) { }
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) { }
                        });
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // Fallback to saved name
                    String savedName = session.getFullname();
                    if (savedName != null && !savedName.isEmpty()) {
                        tvProfile.setText("Hello, " + savedName);
                    }
                }
            });
        } else {
            tvProfile.setText("Hello, User");
        }
    }
}

