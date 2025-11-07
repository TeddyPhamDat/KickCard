package com.example.assignment.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import com.example.assignment.R;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.model.CreatePaymentRequest;
import com.example.assignment.data.model.TopupRequest;
import com.example.assignment.data.repository.PaymentRepository;
import com.example.assignment.ui.PaymentStatusActivity;
import com.example.assignment.utils.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletFragment extends Fragment {
    
    private static final String BASE_URL = "http://10.0.2.2:8080";
    
    private TextView tvWalletBalance;
    private TextView tvSelectedAmount;
    private EditText etCustomAmount;
    private Button btnTopUp, btnTransactionHistory;
    private ProgressBar progressBar;
    
    private PaymentRepository paymentRepository;
    private SessionManager sessionManager;
    private long selectedAmount = 0;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        
        initViews(view);
        initRepositories();
        setupAmountButtons();
        loadWalletBalance();
        
        return view;
    }
    
    private void initViews(View view) {
        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);
        tvSelectedAmount = view.findViewById(R.id.tvSelectedAmount);
        etCustomAmount = view.findViewById(R.id.etCustomAmount);
        btnTopUp = view.findViewById(R.id.btnTopUp);
        btnTransactionHistory = view.findViewById(R.id.btnTransactionHistory);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    private void initRepositories() {
        paymentRepository = new PaymentRepository(RetrofitClient.getInstance(BASE_URL).getApi());
        sessionManager = new SessionManager(requireContext());
    }
    
    private void setupAmountButtons() {
        btnTopUp.setOnClickListener(v -> topUpWallet());
        btnTransactionHistory.setOnClickListener(v -> showTransactionHistory());
        
        etCustomAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    selectedAmount = 0;
                } else {
                    try {
                        selectedAmount = Long.parseLong(s.toString());
                    } catch (NumberFormatException e) {
                        selectedAmount = 0;
                    }
                }
                updateSelectedAmountDisplay();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void updateSelectedAmountDisplay() {
        tvSelectedAmount.setText("Selected: " + formatVND(selectedAmount));
    }
    
    private void loadWalletBalance() {
        String token = "Bearer " + sessionManager.fetchToken();
        paymentRepository.getWalletBalance(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Number balance = (Number) response.body().get("balance");
                        if (balance != null) {
                            tvWalletBalance.setText(formatVND(balance.longValue()));
                        }
                    } catch (Exception e) {
                        tvWalletBalance.setText("Unable to load balance");
                    }
                } else {
                    tvWalletBalance.setText("Unable to load balance");
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                tvWalletBalance.setText("Connection error");
            }
        });
    }
    
    private void topUpWallet() {
        if (selectedAmount <= 0) {
            Toast.makeText(getContext(), "Please select top-up amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate amount range
        if (selectedAmount < 10000) {
            Toast.makeText(getContext(), "Minimum top-up amount is 10,000 VND", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAmount > 10000000) {
            Toast.makeText(getContext(), "Maximum top-up amount is 10,000,000 VND", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        btnTopUp.setEnabled(false);
        
        String token = "Bearer " + sessionManager.fetchToken();
        TopupRequest req = new TopupRequest((double) selectedAmount);
        
        paymentRepository.createWalletTopUp(token, req).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showProgress(false);
                btnTopUp.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    handlePaymentResponse(response.body());
                } else {
                    Toast.makeText(getContext(), "Transaction creation failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showProgress(false);
                btnTopUp.setEnabled(true);
                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void handlePaymentResponse(Map<String, Object> response) {
        try {
            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                // VNPay response format: checkoutUrl, orderCode, amount
                String checkoutUrl = (String) response.get("checkoutUrl");
                String orderCode = (String) response.get("orderCode");
                Double amount = (Double) response.get("amount");
                
                if (checkoutUrl != null && !checkoutUrl.isEmpty()) {
                    // Open VNPay URL directly in browser
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                        startActivity(browserIntent);
                        
                        Toast.makeText(getContext(), "Opening VNPay for payment of " + 
                            String.format("%,d", selectedAmount) + " VND", Toast.LENGTH_LONG).show();
                        
                        // Reset form
                        selectedAmount = 0;
                        etCustomAmount.setText("");
                        updateSelectedAmountDisplay();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "VNPay error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error: Payment URL not received", Toast.LENGTH_SHORT).show();
                }
            } else {
                String error = (String) response.get("error");
                Toast.makeText(getContext(), "Error: " + (error != null ? error : "Unknown"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Response error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showTransactionHistory() {
        Intent intent = new Intent(getActivity(), TransactionHistoryActivity.class);
        startActivity(intent);
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private String formatVND(long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh balance when returning to fragment
        loadWalletBalance();
    }
}
