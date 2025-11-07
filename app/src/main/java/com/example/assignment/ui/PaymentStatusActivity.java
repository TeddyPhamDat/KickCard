package com.example.assignment.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaymentStatusActivity extends AppCompatActivity {
    public static final String EXTRA_ORDER_CODE = "order_code";
    public static final String EXTRA_AMOUNT = "amount";
    
    private TextView tvStatus, tvOrderCode, tvAmount;
    private ProgressBar progressBar;
    private Button btnComplete, btnCancel, btnClose;
    
    private String orderCode;
    private String amount;
    private Handler handler;
    private Runnable pollRunnable;
    private boolean isPolling = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_status);
        
        initViews();
        getExtras();
        setupClickListeners();
        startPolling();
    }
    
    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvAmount = findViewById(R.id.tvAmount);
        progressBar = findViewById(R.id.progressBar);
        btnComplete = findViewById(R.id.btnForceCheck);
        btnCancel = findViewById(R.id.btnCancel);
        btnClose = findViewById(R.id.btnDone);
        
        handler = new Handler(Looper.getMainLooper());
    }
    
    private void getExtras() {
        Intent intent = getIntent();
        orderCode = intent.getStringExtra(EXTRA_ORDER_CODE);
        amount = intent.getStringExtra(EXTRA_AMOUNT);
        
        tvOrderCode.setText("Mã giao dịch: " + (orderCode != null ? orderCode : "N/A"));
        tvAmount.setText("Số tiền: " + (amount != null ? formatAmount(amount) : "N/A"));
    }
    
    private void setupClickListeners() {
        btnComplete.setOnClickListener(v -> completePaymentManually());
        btnCancel.setOnClickListener(v -> cancelPayment());
        btnClose.setOnClickListener(v -> finish());
    }
    
    private void startPolling() {
        if (orderCode == null || isPolling) return;
        
        isPolling = true;
        tvStatus.setText("🔄 Đang kiểm tra trạng thái thanh toán...");
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                checkPaymentStatus();
                if (isPolling) {
                    handler.postDelayed(this, 5000); // Poll every 5 seconds
                }
            }
        };
        handler.post(pollRunnable);
    }
    
    private void stopPolling() {
        isPolling = false;
        if (handler != null && pollRunnable != null) {
            handler.removeCallbacks(pollRunnable);
        }
        progressBar.setVisibility(View.GONE);
    }
    
    private void checkPaymentStatus() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                
                URL url = new URL("http://10.0.2.2:8080/api/payments/status/" + orderCode);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                
                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.getBoolean("success")) {
                        JSONObject data = jsonResponse.getJSONObject("data");
                        String status = data.getString("status");
                        
                        runOnUiThread(() -> {
                            if ("COMPLETED".equals(status)) {
                                showPaymentCompleted();
                            } else if ("PENDING".equals(status)) {
                                if (data.has("remainingTime")) {
                                    try {
                                        long remainingTime = data.getLong("remainingTime");
                                        tvStatus.setText("⏳ Đang xử lý... (còn " + remainingTime + "s)");
                                    } catch (Exception e) {
                                        tvStatus.setText("⏳ Đang xử lý...");
                                    }
                                }
                            }
                        });
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi kiểm tra trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void completePaymentManually() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String token = prefs.getString("token", "");
                
                URL url = new URL("http://10.0.2.2:8080/api/payments/complete/" + orderCode);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                
                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Thanh toán đã được hoàn tất!", Toast.LENGTH_LONG).show();
                        showPaymentCompleted();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Lỗi hoàn tất thanh toán", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void cancelPayment() {
        tvStatus.setText("❌ Thanh toán đã bị hủy");
        tvStatus.setTextColor(getColor(android.R.color.holo_red_light));
        stopPolling();
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }
    
    private void showPaymentCompleted() {
        tvStatus.setText("✅ Thanh toán thành công!");
        tvStatus.setTextColor(getColor(android.R.color.holo_green_light));
        stopPolling();
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        
        // Auto close after 3 seconds
        handler.postDelayed(() -> {
            setResult(RESULT_OK);
            finish();
        }, 3000);
    }
    
    private String formatAmount(String amount) {
        try {
            double amt = Double.parseDouble(amount);
            return String.format("%,.0f VND", amt);
        } catch (NumberFormatException e) {
            return amount + " VND";
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
    }
}