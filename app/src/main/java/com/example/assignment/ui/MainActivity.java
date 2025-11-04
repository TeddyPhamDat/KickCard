package com.example.assignment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.assignment.R;
import com.example.assignment.data.api.ApiService;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.User;
import com.example.assignment.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;
import java.util.Map;

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

        // Initially hide the admin tab. It will be made visible in onResume if the user is an admin.
        bottomNav.getMenu().findItem(R.id.menu_admin).setVisible(false);

        // Set up the listener for when the user clicks a navigation item.
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.menu_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.menu_my_cards) {
                    selectedFragment = new MyCardsFragment();
                } else if (itemId == R.id.menu_wallet) {
                    selectedFragment = new WalletFragment();
                } else if (itemId == R.id.menu_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (itemId == R.id.menu_admin) {
                    selectedFragment = new AdminFragment();
                }

                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                }
                return true;
            }
        });

        // --- CORRECT INITIAL STATE ---
        // Set Home as the default selected item. This will trigger the listener above
        // and load the HomeFragment automatically.
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.menu_home);
        }

        // Header profile name opens ProfileFragment for viewing profile details
        findViewById(R.id.tvProfileName).setOnClickListener(v -> {
            replaceFragment(new ProfileFragment());
            bottomNav.setSelectedItemId(R.id.menu_profile);
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data every time the activity is resumed to catch any changes
        refreshProfileHeader();
    }

    private void refreshProfileHeader() {
        SessionManager session = new SessionManager(this);
        String token = session.fetchToken();
        TextView tvProfile = findViewById(R.id.tvProfileName);

        if (token != null && !token.isEmpty()) {
            ApiService api = RetrofitClient.getApiService(this);
            Call<User> call = api.getCurrentUser("Bearer " + token);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        String displayName = user.getFullname();
                        if (displayName == null || displayName.trim().isEmpty()) {
                            displayName = user.getUsername();
                        }
                        tvProfile.setText("Hello, " + displayName);
                        session.saveProfile(user.getFullname(), user.getPhone(), user.getAddress(), user.getAvatarUrl());
                        session.saveUserId(user.getId());
                        session.saveUsername(user.getUsername());

                        // --- DYNAMIC UI LOGIC BASED ON USER ROLE ---
                        boolean isAdmin = user.getRole() != null && user.getRole().toUpperCase().contains("ADMIN");

                        // Show/hide menu items based on the role
                        bottomNav.getMenu().findItem(R.id.menu_admin).setVisible(isAdmin);
                        bottomNav.getMenu().findItem(R.id.menu_my_cards).setVisible(!isAdmin);
                        bottomNav.getMenu().findItem(R.id.menu_wallet).setVisible(!isAdmin);

                        // This is why you saw the Admin page!
                        // If the user is an admin, override the default and switch to the AdminFragment.
                        if (isAdmin) {
                            replaceFragment(new AdminFragment());
                            bottomNav.setSelectedItemId(R.id.menu_admin);
                        }

                        // Fetch wallet balance for all non-admin users
                        if (!isAdmin) {
                            fetchWalletBalance(token);
                        } else {
                            // Hide balance for admins
                            findViewById(R.id.tvBalance).setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // Fallback to saved name from session if network fails
                    String savedName = session.getFullname();
                    if (savedName != null && !savedName.isEmpty()) {
                        tvProfile.setText("Hello, " + savedName);
                    }
                }
            });
        } else {
            tvProfile.setText("Hello, User");
            // Handle logged out state if necessary
        }
    }

    private void fetchWalletBalance(String token) {
        ApiService api = RetrofitClient.getApiService(this);
        api.getWalletBalance("Bearer " + token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                TextView tvBalance = findViewById(R.id.tvBalance);
                if (response.isSuccessful() && response.body() != null) {
                    Object balanceObj = response.body().get("balance");
                    try {
                        double balance = Double.parseDouble(balanceObj.toString());
                        SessionManager session = new SessionManager(MainActivity.this);
                        session.saveWalletBalance(balance);
                        tvBalance.setVisibility(View.VISIBLE);
                        tvBalance.setText("Balance: $" + String.format(Locale.US, "%.2f", balance));
                    } catch (Exception e) {
                        tvBalance.setVisibility(View.GONE);
                    }
                } else {
                    tvBalance.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                findViewById(R.id.tvBalance).setVisibility(View.GONE);
            }
        });
    }
}