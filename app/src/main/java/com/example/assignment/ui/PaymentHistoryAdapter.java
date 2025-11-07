package com.example.assignment.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    private List<Map<String, Object>> payments;
    private Context context;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public PaymentHistoryAdapter(List<Map<String, Object>> payments, Context context) {
        this.payments = payments;
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_history, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Map<String, Object> payment = payments.get(position);

        try {
            // Order code
            String orderCode = (String) payment.get("orderCode");
            holder.tvOrderCode.setText("Mã đơn: " + orderCode);

            // Amount
            Double amount = (Double) payment.get("amount");
            if (amount != null) {
                holder.tvAmount.setText(currencyFormat.format(amount));
            }

            // Status
            String status = (String) payment.get("status");
            holder.tvStatus.setText(getStatusText(status));
            holder.tvStatus.setTextColor(getStatusColor(status));

            // Description
            String description = (String) payment.get("description");
            if (description != null) {
                holder.tvDescription.setText(description);
            }

            // Date
            String createdAt = (String) payment.get("createdAt");
            if (createdAt != null) {
                try {
                    // Parse ISO date string và format lại
                    holder.tvDate.setText(createdAt.substring(0, 16).replace("T", " "));
                } catch (Exception e) {
                    holder.tvDate.setText(createdAt);
                }
            }

        } catch (Exception e) {
            // Handle parsing errors
            holder.tvOrderCode.setText("Lỗi dữ liệu");
        }
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";

        switch (status.toUpperCase()) {
            case "PENDING": return "Chờ thanh toán";
            case "PAID": return "Đã thanh toán";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.black;

        switch (status.toUpperCase()) {
            case "PENDING": return R.color.colorAccent;
            case "PAID": return R.color.colorSuccess;
            case "CANCELLED": return R.color.colorError;
            default: return R.color.black;
        }
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvAmount, tvStatus, tvDescription, tvDate;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
