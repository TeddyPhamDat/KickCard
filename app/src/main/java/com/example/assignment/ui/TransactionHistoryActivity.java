package com.example.assignment.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.api.RetrofitClient;
import com.example.assignment.data.repository.PaymentRepository;
import com.example.assignment.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080";

    private RecyclerView rvTransactions;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TransactionAdapter adapter;
    private PaymentRepository paymentRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Transaction History");
        }

        // Initialize views
        rvTransactions = findViewById(R.id.rvTransactions);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>());
        rvTransactions.setAdapter(adapter);

        // Initialize repositories
        sessionManager = new SessionManager(this);
        paymentRepository = new PaymentRepository(RetrofitClient.getInstance(BASE_URL).getApi());

        // Load transactions
        loadTransactions();
    }

    private void loadTransactions() {
        showProgress(true);
        String token = "Bearer " + sessionManager.fetchToken();

        paymentRepository.getWalletTransactions(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<Map<String, Object>> transactionsData = 
                            (List<Map<String, Object>>) response.body().get("transactions");
                        
                        if (transactionsData != null && !transactionsData.isEmpty()) {
                            List<TransactionItem> items = new ArrayList<>();
                            for (Map<String, Object> tx : transactionsData) {
                                TransactionItem item = new TransactionItem();
                                item.id = ((Number) tx.get("id")).longValue();
                                item.orderCode = (String) tx.get("orderCode");
                                item.amount = ((Number) tx.get("amount")).doubleValue();
                                item.type = (String) tx.get("type");
                                item.status = (String) tx.get("status");
                                item.paymentMethod = (String) tx.get("paymentMethod");
                                item.description = (String) tx.get("description");
                                item.transactionNo = (String) tx.get("transactionNo");
                                item.bankCode = (String) tx.get("bankCode");
                                item.createdAt = (String) tx.get("createdAt");
                                item.completedAt = (String) tx.get("completedAt");
                                items.add(item);
                            }
                            
                            adapter.updateData(items);
                            tvEmptyState.setVisibility(View.GONE);
                            rvTransactions.setVisibility(View.VISIBLE);
                        } else {
                            showEmptyState();
                        }
                    } catch (Exception e) {
                        Toast.makeText(TransactionHistoryActivity.this, 
                            "Error parsing transactions: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(TransactionHistoryActivity.this, 
                        "Failed to load transactions", 
                        Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(TransactionHistoryActivity.this, 
                    "Connection error: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvTransactions.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvTransactions.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner class for transaction item
    static class TransactionItem {
        long id;
        String orderCode;
        double amount;
        String type;
        String status;
        String paymentMethod;
        String description;
        String transactionNo;
        String bankCode;
        String createdAt;
        String completedAt;
    }

    // Adapter for RecyclerView
    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private List<TransactionItem> transactions;
        private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        private SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        TransactionAdapter(List<TransactionItem> transactions) {
            this.transactions = transactions;
        }

        void updateData(List<TransactionItem> newData) {
            this.transactions = newData;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TransactionItem item = transactions.get(position);
            
            // Format amount
            holder.tvAmount.setText(formatVND(item.amount));
            holder.tvAmount.setTextColor(getResources().getColor(
                "COMPLETED".equals(item.status) ? android.R.color.holo_green_dark : android.R.color.darker_gray
            ));
            
            // Set type and description
            holder.tvType.setText(getTypeDisplay(item.type));
            holder.tvDescription.setText(item.description != null ? item.description : "Wallet transaction");
            
            // Set status
            holder.tvStatus.setText(getStatusDisplay(item.status));
            holder.tvStatus.setTextColor(getStatusColor(item.status));
            
            // Format date
            String dateStr = item.completedAt != null ? item.completedAt : item.createdAt;
            holder.tvDate.setText(formatDate(dateStr));
            
            // Payment method
            if (item.paymentMethod != null) {
                holder.tvPaymentMethod.setVisibility(View.VISIBLE);
                holder.tvPaymentMethod.setText(item.paymentMethod);
            } else {
                holder.tvPaymentMethod.setVisibility(View.GONE);
            }
            
            // Transaction number
            if (item.transactionNo != null && !item.transactionNo.isEmpty()) {
                holder.tvTransactionNo.setVisibility(View.VISIBLE);
                holder.tvTransactionNo.setText("Txn: " + item.transactionNo);
            } else {
                holder.tvTransactionNo.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        private String formatVND(double amount) {
            return String.format(Locale.getDefault(), "+%,.0f ₫", amount);
        }

        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "";
            try {
                // Handle both formats with and without milliseconds
                SimpleDateFormat parser;
                if (dateStr.contains(".")) {
                    parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                } else {
                    parser = inputFormat;
                }
                Date date = parser.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateStr;
            }
        }

        private String getTypeDisplay(String type) {
            switch (type) {
                case "TOPUP": return "Top Up";
                case "PURCHASE": return "Purchase";
                case "REFUND": return "Refund";
                default: return type;
            }
        }

        private String getStatusDisplay(String status) {
            switch (status) {
                case "COMPLETED": return "✓ Completed";
                case "PENDING": return "⏳ Pending";
                case "FAILED": return "✗ Failed";
                case "CANCELLED": return "✗ Cancelled";
                default: return status;
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "COMPLETED": 
                    return getResources().getColor(android.R.color.holo_green_dark);
                case "PENDING": 
                    return getResources().getColor(android.R.color.holo_orange_dark);
                case "FAILED":
                case "CANCELLED":
                    return getResources().getColor(android.R.color.holo_red_dark);
                default: 
                    return getResources().getColor(android.R.color.darker_gray);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAmount, tvType, tvDescription, tvStatus, tvDate, tvPaymentMethod, tvTransactionNo;

            ViewHolder(View itemView) {
                super(itemView);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvType = itemView.findViewById(R.id.tvType);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
                tvTransactionNo = itemView.findViewById(R.id.tvTransactionNo);
            }
        }
    }
}
