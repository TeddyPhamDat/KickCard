package com.example.assignment.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.model.Card;
import com.example.assignment.data.model.CreatePaymentRequest;
import com.example.assignment.data.repository.HomeRepository;
import com.example.assignment.data.repository.PaymentRepository;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {
    public static final String EXTRA_CARD_ID = "card_id";
    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final int POLLING_INTERVAL_MS = 5000;

    // UI Components - Common
    private TabLayout tabLayout;
    private FrameLayout frameContent;
    private LinearLayout layoutCard, layoutWallet;
    private Button btnPayNow, btnCheckStatus, btnCancel, btnTopUp;

    // UI Components - Card Tab
    private ImageView imgCard;
    private TextView tvCardName, tvCardPrice, tvPaymentStatus, tvOrderCode;
    private ProgressBar progressBar, progressStatus;
    private MaterialCardView cardInfo, paymentInfo;
    private Button btnCopyOrderCode;

    // UI Components - Wallet Tab
    private TextView tvWalletBalance, tvSelectedAmount;
    private EditText etCustomAmount;
    private long selectedWalletAmount = 0;

    // Data
    private Card currentCard;
    private PaymentRepository paymentRepository;
    private SessionManager sessionManager;
    private String currentOrderCode;
    private Handler statusHandler;
    private Runnable statusRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        setupToolbar();
        initViews();
        initRepositories();
        setupTabListener();

        Intent intent = getIntent();
        if (handleDeepLink(intent)) return;

        Long cardId = intent.getLongExtra(EXTRA_CARD_ID, -1);
        if (cardId != -1) loadCard(cardId);
        else {
            tabLayout.selectTab(tabLayout.getTabAt(1));
            loadWalletBalance();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        frameContent = findViewById(R.id.frameContent);
        layoutCard = findViewById(R.id.layoutCard);
        layoutWallet = findViewById(R.id.layoutWallet);

        imgCard = findViewById(R.id.imgCard);
        tvCardName = findViewById(R.id.tvCardName);
        tvCardPrice = findViewById(R.id.tvCardPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvOrderCode = findViewById(R.id.tvOrderCode);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        btnCancel = findViewById(R.id.btnCancel);
        btnCopyOrderCode = findViewById(R.id.btnCopyOrderCode);
        btnTopUp = findViewById(R.id.btnTopUp);
        progressBar = findViewById(R.id.progressBar);
        progressStatus = findViewById(R.id.progressStatus);
        cardInfo = findViewById(R.id.cardInfo);
        paymentInfo = findViewById(R.id.paymentInfo);

        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvSelectedAmount = findViewById(R.id.tvSelectedAmount);
        etCustomAmount = findViewById(R.id.etCustomAmount);

        btnPayNow.setOnClickListener(v -> createPayment());
        btnCheckStatus.setOnClickListener(v -> checkPaymentStatus());
        btnCancel.setOnClickListener(v -> finish());
        btnCopyOrderCode.setOnClickListener(v -> copyOrderCode());
        btnTopUp.setOnClickListener(v -> createWalletPayment());

        setupWalletAmountButtons();

        statusHandler = new Handler(Looper.getMainLooper());
    }

    private void setupWalletAmountButtons() {
        findViewById(R.id.btn50k).setOnClickListener(v -> selectWalletAmount(50000));
        findViewById(R.id.btn100k).setOnClickListener(v -> selectWalletAmount(100000));
        findViewById(R.id.btn200k).setOnClickListener(v -> selectWalletAmount(200000));
        findViewById(R.id.btn500k).setOnClickListener(v -> selectWalletAmount(500000));

        etCustomAmount.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    selectedWalletAmount = 0;
                } else {
                    try {
                        selectedWalletAmount = Long.parseLong(s.toString());
                    } catch (NumberFormatException e) {
                        selectedWalletAmount = 0;
                    }
                }
                updateSelectedAmountDisplay();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void selectWalletAmount(long amount) {
        selectedWalletAmount = amount;
        etCustomAmount.setText("");
        updateSelectedAmountDisplay();
        Toast.makeText(this, "Selected: " + formatVND(amount), Toast.LENGTH_SHORT).show();
    }

    private void updateSelectedAmountDisplay() {
        tvSelectedAmount.setText(formatVND(selectedWalletAmount));
    }

    private void setupTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    layoutCard.setVisibility(View.VISIBLE);
                    layoutWallet.setVisibility(View.GONE);
                    btnPayNow.setVisibility(currentCard != null ? View.VISIBLE : View.GONE);
                    btnTopUp.setVisibility(View.GONE);
                } else {
                    layoutCard.setVisibility(View.GONE);
                    layoutWallet.setVisibility(View.VISIBLE);
                    btnPayNow.setVisibility(View.GONE);
                    btnTopUp.setVisibility(View.VISIBLE);
                    loadWalletBalance();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initRepositories() {
        paymentRepository = new PaymentRepository(RetrofitClient.getInstance(BASE_URL).getApi());
        sessionManager = new SessionManager(this);
    }

    private void loadCard(Long cardId) {
        HomeRepository homeRepository = new HomeRepository(this, BASE_URL);
        showProgress(true);
        homeRepository.getHomeCard(cardId).enqueue(new Callback<Card>() {
            @Override public void onResponse(Call<Card> call, Response<Card> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentCard = response.body();
                    bindCardData(currentCard);
                } else {
                    Toast.makeText(PaymentActivity.this, "Error loading card", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override public void onFailure(Call<Card> call, Throwable t) {
                showProgress(false);
                Toast.makeText(PaymentActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindCardData(Card card) {
        Glide.with(this).load(card.getBaseImageUrl()).placeholder(R.drawable.ic_soccer_ball).into(imgCard);
        tvCardName.setText(card.getName());
        tvCardPrice.setText(formatVND(card.getPrice().longValue()));
        cardInfo.setVisibility(View.VISIBLE);
        btnPayNow.setEnabled(true);
    }

    private void createPayment() {
        if (currentCard == null) return;
        CreatePaymentRequest req = new CreatePaymentRequest(currentCard.getId(), currentCard.getPrice(), "Buy: " + currentCard.getName());
        showProgress(true);
        btnPayNow.setEnabled(false);
        String token = "Bearer " + sessionManager.fetchToken();
        paymentRepository.createPayment(token, req).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    handlePaymentResponse(response.body());
                } else {
                    btnPayNow.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "Create payment failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showProgress(false);
                btnPayNow.setEnabled(true);
                Toast.makeText(PaymentActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createWalletPayment() {
        if (selectedWalletAmount <= 0) {
            Toast.makeText(this, "Select amount", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress(true);
        btnTopUp.setEnabled(false);
        String token = "Bearer " + sessionManager.fetchToken();
        CreatePaymentRequest req = new CreatePaymentRequest(0L, (double) selectedWalletAmount, "Wallet Top-up");
        paymentRepository.createWalletPayment(token, req).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    handleWalletPaymentResponse(response.body());
                } else {
                    btnTopUp.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "Create top-up failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showProgress(false);
                btnTopUp.setEnabled(true);
                Toast.makeText(PaymentActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handlePaymentResponse(Map<String, Object> response) {
        try {
            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                Map<String, Object> payment = (Map<String, Object>) response.get("payment");
                if (payment != null) {
                    currentOrderCode = (String) payment.get("orderCode");
                    String paymentUrl = (String) payment.get("paymentUrl");
                    tvOrderCode.setText(currentOrderCode);
                    tvPaymentStatus.setText("Waiting payment...");
                    paymentInfo.setVisibility(View.VISIBLE);
                    btnPayNow.setVisibility(View.GONE);
                    btnCheckStatus.setVisibility(View.VISIBLE);
                    if (paymentUrl != null && !paymentUrl.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl)));
                        startStatusPolling();
                    } else {
                        Toast.makeText(this, "PayOS URL empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Response error", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unchecked")
    private void handleWalletPaymentResponse(Map<String, Object> response) {
        try {
            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                Map<String, Object> payment = (Map<String, Object>) response.get("payment");
                if (payment != null) {
                    String paymentUrl = (String) payment.get("paymentUrl");
                    if (paymentUrl != null && !paymentUrl.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl)));
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Response error", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWalletBalance() {
        String token = "Bearer " + sessionManager.fetchToken();
        paymentRepository.getWalletBalance(token).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Number balance = (Number) response.body().get("balance");
                        if (balance != null) {
                            tvWalletBalance.setText(formatVND(balance.longValue()));
                        }
                    } catch (Exception e) {}
                }
            }

            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void startStatusPolling() {
        statusRunnable = () -> {
            checkPaymentStatus();
            statusHandler.postDelayed(statusRunnable, POLLING_INTERVAL_MS);
        };
        statusHandler.postDelayed(statusRunnable, 3000);
    }

    private void stopStatusPolling() {
        if (statusHandler != null && statusRunnable != null) {
            statusHandler.removeCallbacks(statusRunnable);
            statusRunnable = null;
        }
    }

    private void checkPaymentStatus() {
        if (currentOrderCode == null) return;
        // Disabled for VNPay - not needed since VNPay redirects back automatically
        Toast.makeText(this, "VNPay sẽ tự động redirect về app sau khi thanh toán", Toast.LENGTH_SHORT).show();
        /*
        String token = "Bearer " + sessionManager.fetchToken();
        paymentRepository.checkPaymentStatus(token, currentOrderCode).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleStatusResponse(response.body());
                }
            }

            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
        */
    }

    @SuppressWarnings("unchecked")
    private void handleStatusResponse(Map<String, Object> response) {
        try {
            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                String status = (String) response.get("status");
                if (status != null) {
                    if ("PAID".equalsIgnoreCase(status)) {
                        stopStatusPolling();
                        showSuccess();
                    } else if ("CANCELLED".equalsIgnoreCase(status)) {
                        stopStatusPolling();
                        showCancelled();
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void showSuccess() {
        tvPaymentStatus.setText("SUCCESS");
        Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();
        btnCheckStatus.setVisibility(View.GONE);
        progressStatus.setVisibility(View.GONE);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Success")
                .setMessage("Payment completed.")
                .setPositiveButton("OK", (d, w) -> { setResult(RESULT_OK); finish(); })
                .setCancelable(false).show();
    }

    private void showCancelled() {
        tvPaymentStatus.setText("CANCELLED");
        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
        btnCheckStatus.setVisibility(View.GONE);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancelled")
                .setMessage("Try again?")
                .setPositiveButton("Retry", (d, w) -> { btnPayNow.setVisibility(View.VISIBLE); btnPayNow.setEnabled(true); paymentInfo.setVisibility(View.GONE); currentOrderCode = null; })
                .setNegativeButton("Home", (d, w) -> finish())
                .show();
    }

    private boolean handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "kickcard".equals(data.getScheme()) && "payment".equals(data.getHost())) {
            String path = data.getPath();
            if ("/success".equals(path)) { 
                // VNPay đã redirect về với thông tin đầy đủ
                String orderCode = data.getQueryParameter("orderCode");
                String amountStr = data.getQueryParameter("amount");
                String responseCode = data.getQueryParameter("responseCode");
                String transactionNo = data.getQueryParameter("transactionNo");
                
                if ("00".equals(responseCode) && orderCode != null && amountStr != null) {
                    try {
                        long amount = Long.parseLong(amountStr);
                        showPaymentSuccess(orderCode, amount, transactionNo);
                    } catch (NumberFormatException e) {
                        showPaymentSuccess(orderCode, 0, transactionNo);
                    }
                } else {
                    showSuccess(); // Fallback
                }
                return true; 
            }
            else if ("/cancel".equals(path)) { 
                String orderCode = data.getQueryParameter("orderCode");
                String responseCode = data.getQueryParameter("responseCode");
                showPaymentFailed(orderCode, responseCode);
                return true; 
            }
        }
        return false;
    }

    private void showPaymentSuccess(String orderCode, long amount, String transactionNo) {
        runOnUiThread(() -> {
            // Update UI
            tvPaymentStatus.setText("✅ Nạp tiền thành công!");
            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            
            if (amount > 0) {
                tvSelectedAmount.setText("Số tiền: " + formatVND(amount));
            }
            if (orderCode != null) {
                tvOrderCode.setText("Mã giao dịch: " + orderCode);
            }
            
            // Hide progress
            showProgress(false);
            
            // Show success toast
            Toast.makeText(this, "🎉 Nạp tiền thành công! Số dư ví đã được cập nhật.", Toast.LENGTH_LONG).show();
            
            // Update local wallet balance by fetching from server
            refreshWalletBalance();
            
            // Auto finish after 3 seconds
            new android.os.Handler().postDelayed(() -> {
                setResult(RESULT_OK);
                finish();
            }, 3000);
        });
    }

    private void showPaymentFailed(String orderCode, String responseCode) {
        runOnUiThread(() -> {
            tvPaymentStatus.setText("❌ Thanh toán thất bại");
            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            
            if (orderCode != null) {
                tvOrderCode.setText("Mã giao dịch: " + orderCode);
            }
            
            showProgress(false);
            Toast.makeText(this, "Thanh toán không thành công. Mã lỗi: " + responseCode, Toast.LENGTH_LONG).show();
        });
    }

    private void refreshWalletBalance() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                
                java.net.URL url = new java.net.URL("http://10.0.2.2:8080/api/users/me");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                
                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                    if (jsonResponse.getBoolean("success")) {
                        org.json.JSONObject userData = jsonResponse.getJSONObject("data");
                        double newBalance = userData.getDouble("walletBalance");
                        
                        // Update SharedPreferences
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("walletBalance", String.valueOf(newBalance));
                        editor.apply();
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "💰 Số dư ví: " + formatVND((long)newBalance), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void copyOrderCode() {
        if (currentOrderCode == null) return;
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("OrderCode", currentOrderCode);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String formatVND(long amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    @Override protected void onResume() { 
        super.onResume(); 
        if (currentOrderCode != null) { 
            checkPaymentStatus(); 
            if (statusRunnable == null) startStatusPolling(); 
        } 
    }
    @Override protected void onPause() { super.onPause(); stopStatusPolling(); }
    @Override protected void onDestroy() { super.onDestroy(); stopStatusPolling(); }
}

