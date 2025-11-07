package com.example.assignment.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment.R;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.repository.PaymentRepository;
import com.example.assignment.utils.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity hiển thị trạng thái thanh toán với polling
 * 
 * Luồng:
 * 1. Nhận orderCode, amount, paymentUrl từ intent
 * 2. Mở VNPay trong browser
 * 3. Polling check status mỗi 3 giây
 * 4. Hiển thị PENDING → PAID → Success
 */
public class PaymentStatusActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final int POLLING_INTERVAL = 3000; // 3 seconds
    private static final int MAX_POLLING_ATTEMPTS = 40; // 2 minutes total

    // Views
    private TextView tvTitle;
    private TextView tvAmount;
    private TextView tvOrderCode;
    private TextView tvStatus;
    private ImageView ivStatusIcon;
    private ProgressBar progressBar;
    private ProgressBar progressPolling;
    private Button btnOpenVNPay;
    private Button btnCheckStatus;
    private Button btnForceCheck;
    private Button btnDone;
    private Button btnCancel;

    // Data
    private PaymentRepository paymentRepository;
    private SessionManager sessionManager;
    private Handler pollingHandler;
    private Runnable pollingRunnable;
    
    private String orderCode;
    private String paymentUrl;
    private Double amount;
    private String paymentType; // "topup" or "card"
    private int pollingAttempts = 0;
    private boolean isPolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_status);
        
        initViews();
        initData();
        getIntentData();
        setupToolbar();
        setupButtons();
        
        // Tự động mở PayOS
        openVNPay();
        
        // Bắt đầu polling sau 5 giây
        startPollingWithDelay();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvAmount = findViewById(R.id.tvAmount);
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvStatus = findViewById(R.id.tvStatus);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        progressBar = findViewById(R.id.progressBar);
        progressPolling = findViewById(R.id.progressPolling);
        btnOpenVNPay = findViewById(R.id.btnOpenPayOS);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        btnForceCheck = findViewById(R.id.btnForceCheck);
        btnDone = findViewById(R.id.btnDone);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initData() {
        paymentRepository = new PaymentRepository(RetrofitClient.getInstance(BASE_URL).getApi());
        sessionManager = new SessionManager(this);
        pollingHandler = new Handler(Looper.getMainLooper());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        orderCode = intent.getStringExtra("orderCode");
        paymentUrl = intent.getStringExtra("paymentUrl");
        amount = intent.getDoubleExtra("amount", 0);
        paymentType = intent.getStringExtra("paymentType"); // "topup" or "card"

        if (orderCode == null || paymentUrl == null) {
            Toast.makeText(this, "Dữ liệu thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị thông tin
        tvOrderCode.setText("Mã đơn hàng: " + orderCode);
        tvAmount.setText(formatVND(amount.longValue()));
        
        String title = "topup".equals(paymentType) ? "Nạp tiền vào ví" : "Thanh toán mua thẻ";
        tvTitle.setText(title);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trạng thái thanh toán");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupButtons() {
        btnOpenVNPay.setOnClickListener(v -> openVNPay());
        btnCheckStatus.setOnClickListener(v -> {
            Toast.makeText(this, "VNPay sẽ tự động redirect về app sau khi thanh toán thành công", Toast.LENGTH_LONG).show();
        });
        btnForceCheck.setOnClickListener(v -> {
            Toast.makeText(this, "VNPay không cần force check - sẽ tự động cập nhật", Toast.LENGTH_LONG).show();
        });
        btnDone.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> {
            stopPolling();
            finish();
        });
    }

    private void openVNPay() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(intent);
            
            updateStatus("Đang chờ thanh toán...", "PENDING", R.drawable.ic_hourglass_empty);
            showProgress(true);
            
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở trang thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPollingWithDelay() {
        pollingHandler.postDelayed(() -> {
            Toast.makeText(this, "Bắt đầu kiểm tra trạng thái thanh toán...", Toast.LENGTH_SHORT).show();
            startPolling();
        }, 5000); // Delay 5 giây
    }

    private void startPolling() {
        if (isPolling) return;
        
        isPolling = true;
        pollingAttempts = 0;
        progressPolling.setVisibility(View.VISIBLE);
        
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (pollingAttempts >= MAX_POLLING_ATTEMPTS) {
                    stopPolling();
                    updateStatus("Hết thời gian chờ", "TIMEOUT", R.drawable.ic_error);
                    Toast.makeText(PaymentStatusActivity.this, "Hết thời gian chờ. Vui lòng kiểm tra lại.", Toast.LENGTH_LONG).show();
                    return;
                }
                
                pollingAttempts++;
                // checkPaymentStatusSilent(); // Disabled for VNPay
                
                if (isPolling) {
                    pollingHandler.postDelayed(this, POLLING_INTERVAL);
                }
            }
        };
        
        pollingHandler.post(pollingRunnable);
    }

    private void stopPolling() {
        isPolling = false;
        if (pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
        progressPolling.setVisibility(View.GONE);
    }

    private void checkPaymentStatus() {
        showProgress(true);
        // checkPaymentStatusSilent(); // Disabled for VNPay
    }

    /*
    // Disabled for VNPay - not needed since VNPay redirects back automatically
    private void checkPaymentStatusSilent() {
        Toast.makeText(this, "VNPay không cần check status - sẽ tự động redirect", Toast.LENGTH_SHORT).show();
    }
    */

    /*
    // Disabled for VNPay - force check not needed
    private void forceCheckPayment() {
        Toast.makeText(this, "VNPay không cần force check - sẽ tự động redirect", Toast.LENGTH_SHORT).show();
    }
    */

    @SuppressWarnings("unchecked")
    private void handleStatusResponse(Map<String, Object> response) {
        try {
            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                // Status nằm trong data object
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String status = data != null ? (String) data.get("status") : null;
                
                System.out.println("[PaymentStatus] Received response: " + response);
                System.out.println("[PaymentStatus] Received status: " + status);
                
                switch (status != null ? status : "UNKNOWN") {
                    case "PAID":
                        stopPolling();
                        updateStatus("Thanh toán thành công! 🎉", "PAID", R.drawable.ic_check_circle);
                        showSuccessButtons();
                        
                        if ("topup".equals(paymentType)) {
                            Toast.makeText(this, "Nạp tiền thành công! Số dư đã được cập nhật.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Mua thẻ thành công!", Toast.LENGTH_LONG).show();
                        }
                        break;
                        
                    case "PENDING":
                        System.out.println("[PaymentStatus] Still pending, continue polling... (attempt " + pollingAttempts + "/" + MAX_POLLING_ATTEMPTS + ")");
                        updateStatus("Đang chờ thanh toán... (" + pollingAttempts + "/" + MAX_POLLING_ATTEMPTS + ")", "PENDING", R.drawable.ic_hourglass_empty);
                        // Tiếp tục polling
                        break;
                        
                    case "CANCELLED":
                        stopPolling();
                        updateStatus("Đã hủy thanh toán", "CANCELLED", R.drawable.ic_cancel);
                        showCancelButtons();
                        break;
                        
                    default:
                        updateStatus("Trạng thái: " + status, status, R.drawable.ic_help);
                        break;
                }
            } else {
                String error = (String) response.get("error");
                updateStatus("Lỗi: " + error, "ERROR", R.drawable.ic_error);
            }
        } catch (Exception e) {
            updateStatus("Lỗi xử lý phản hồi", "ERROR", R.drawable.ic_error);
        }
    }

    private void updateStatus(String message, String status, int iconResId) {
        tvStatus.setText(message);
        ivStatusIcon.setImageResource(iconResId);
        
        // Cập nhật màu sắc
        int color;
        switch (status) {
            case "PAID":
                color = getColor(android.R.color.holo_green_dark);
                break;
            case "CANCELLED":
            case "ERROR":
                color = getColor(android.R.color.holo_red_dark);
                break;
            case "PENDING":
                color = getColor(android.R.color.holo_orange_dark);
                break;
            default:
                color = getColor(android.R.color.darker_gray);
                break;
        }
        
        tvStatus.setTextColor(color);
        ivStatusIcon.setColorFilter(color);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showSuccessButtons() {
        btnOpenVNPay.setVisibility(View.GONE);
        btnCheckStatus.setVisibility(View.GONE);
        btnForceCheck.setVisibility(View.GONE);
        btnDone.setVisibility(View.VISIBLE);
        btnCancel.setText("Đóng");
    }

    private void showCancelButtons() {
        btnOpenVNPay.setVisibility(View.VISIBLE);
        btnCheckStatus.setVisibility(View.VISIBLE);
        btnForceCheck.setVisibility(View.VISIBLE);
        btnDone.setVisibility(View.GONE);
        btnCancel.setText("Hủy");
    }

    private String formatVND(long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // VNPay will redirect back to app automatically, no need to check status
        Toast.makeText(this, "Nếu bạn đã thanh toán, VNPay sẽ tự động cập nhật kết quả", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
    }

    @Override
    public boolean onSupportNavigateUp() {
        stopPolling();
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        stopPolling();
        super.onBackPressed();
    }
}