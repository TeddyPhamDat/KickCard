package com.example.assignment.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.model.MyCard;

import java.util.ArrayList;
import java.util.List;

public class MyCardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onEdit(MyCard card);
        void onDelete(MyCard card);
        void onCardClick(MyCard card);
    }
    public interface ResellListener extends Listener {
        void onResell(MyCard card);
        void onRetryResell(MyCard card);
        void onCancelResell(MyCard card);
    }

    private List<MyCard> items = new ArrayList<>();
    private Listener listener;

    public MyCardsAdapter(Listener listener) { this.listener = listener; }

    public void setItems(List<MyCard> items) { this.items = items == null ? new ArrayList<>() : items; notifyDataSetChanged(); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyCard c = items.get(position);
        VH vh = (VH) holder;

        // Set card name
        vh.name.setText(c.getName() == null ? "Unnamed" : c.getName());

        // Set status with color
        String status = c.getStatus() == null ? "UNKNOWN" : c.getStatus();
        vh.status.setText(status);
        setStatusColor(vh.status, status);

        // Set rarity and team
        String rarityTeam = "";
        if (c.getRarity() != null) rarityTeam += c.getRarity();
        if (c.getTeam() != null) {
            if (!rarityTeam.isEmpty()) rarityTeam += " • ";
            rarityTeam += c.getTeam();
        }
        vh.rarity.setText(rarityTeam);

        // Set price if available
        if (c.getPrice() != null && c.getPrice() > 0) {
            vh.price.setText(String.format("$%.2f", c.getPrice()));
            vh.price.setVisibility(View.VISIBLE);
        } else {
            vh.price.setVisibility(View.GONE);
        }

        // Set rejection reason if rejected
        if ("REJECTED".equalsIgnoreCase(status) && c.getRejectionReason() != null) {
            vh.rejectionReason.setText("Lý do: " + c.getRejectionReason());
            vh.rejectionReason.setVisibility(View.VISIBLE);
        } else {
            vh.rejectionReason.setVisibility(View.GONE);
        }

        // Load image
        if (c.getBaseImageUrl() != null && !c.getBaseImageUrl().isEmpty()) {
            Glide.with(vh.img.getContext()).load(c.getBaseImageUrl()).into(vh.img);
        } else {
            vh.img.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Edit button: visible for PENDING, APPROVED, SOLD cards (backend allows these)
        boolean canEdit = "PENDING".equals(status) || "APPROVED".equals(status) || "SOLD".equals(status);
        vh.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        // Delete button: only visible for PENDING cards
        boolean canDelete = "PENDING".equals(status);
        vh.btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);

        // Resell button: visible for APPROVED and SOLD cards
        boolean canResell = "APPROVED".equals(status) || "SOLD".equals(status);
        vh.btnResell.setVisibility(canResell ? View.VISIBLE : View.GONE);

        // Retry Resell button: visible for REJECTED cards
        boolean isRejected = "REJECTED".equals(status);
        vh.btnRetryResell.setVisibility(isRejected ? View.VISIBLE : View.GONE);
        
        // Cancel Resell button: visible for REJECTED and PENDING (resell) cards
        boolean canCancelResell = "REJECTED".equals(status) || ("PENDING".equals(status));
        vh.btnCancelResell.setVisibility(canCancelResell ? View.VISIBLE : View.GONE);

        vh.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(c); });
        vh.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(c); });
        vh.btnResell.setOnClickListener(v -> {
            if (listener instanceof MyCardsAdapter.ResellListener) {
                ((ResellListener) listener).onResell(c);
            }
        });
        vh.btnRetryResell.setOnClickListener(v -> {
            if (listener instanceof MyCardsAdapter.ResellListener) {
                ((ResellListener) listener).onRetryResell(c);
            }
        });
        vh.btnCancelResell.setOnClickListener(v -> {
            if (listener instanceof MyCardsAdapter.ResellListener) {
                ((ResellListener) listener).onCancelResell(c);
            }
        });

        // Sự kiện click vào toàn bộ item để xem chi tiết
        vh.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(c);
        });
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    private void setStatusColor(TextView statusView, String status) {
        int color;
        switch (status.toUpperCase()) {
            case "PENDING":
                color = 0xFFFF9800; // Orange
                break;
            case "APPROVED":
                color = 0xFF4CAF50; // Green
                break;
            case "REJECTED":
                color = 0xFFF44336; // Red
                break;
            case "SOLD":
                color = 0xFF2196F3; // Blue
                break;
            default:
                color = 0xFF9E9E9E; // Gray
                break;
        }
        statusView.setBackgroundColor(color);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, status, rarity, price, rejectionReason;
        ImageButton btnEdit, btnDelete, btnResell, btnRetryResell, btnCancelResell;

        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgThumb);
            name = itemView.findViewById(R.id.tvName);
            status = itemView.findViewById(R.id.tvStatus);
            rarity = itemView.findViewById(R.id.tvRarity);
            price = itemView.findViewById(R.id.tvPrice);
            rejectionReason = itemView.findViewById(R.id.tvRejectionReason);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnResell = itemView.findViewById(R.id.btnResell);
            btnRetryResell = itemView.findViewById(R.id.btnRetryResell);
            btnCancelResell = itemView.findViewById(R.id.btnCancelResell);
        }
    }
}
