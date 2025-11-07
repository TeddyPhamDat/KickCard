package com.example.assignment.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.model.MyOwnedCard;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private List<MyOwnedCard> items = new ArrayList<>();
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public void setItems(List<MyOwnedCard> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_card, parent, false);
        return new InventoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        MyOwnedCard card = items.get(position);
        
        // Card Name
        holder.tvName.setText(card.getName());
        
        // Rarity
        String rarity = card.getRarity();
        if (rarity != null) {
            holder.tvRarity.setText(rarity.toUpperCase());
            holder.tvRarity.setBackgroundResource(getRarityBackground(rarity));
            holder.tvRarity.setTextColor(holder.itemView.getContext().getColor(getRarityColor(rarity)));
        } else {
            holder.tvRarity.setText("COMMON");
        }
        
        // Team
        String team = card.getTeam();
        if (team != null && !team.isEmpty()) {
            holder.tvTeam.setText("Team: " + team);
            holder.tvTeam.setVisibility(View.VISIBLE);
        } else {
            holder.tvTeam.setVisibility(View.GONE);
        }
        
        // Status
        String status = card.getStatus() != null ? card.getStatus() : "OWNED";
        holder.tvStatus.setText(status);
        holder.tvStatus.setBackgroundResource(getStatusBackground(status));
        holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(getStatusColor(status)));
        
        // Price
        Double price = card.getPrice();
        if (price != null && price > 0) {
            holder.tvPrice.setText(String.format("VND %,.0f", price));
            holder.tvPrice.setVisibility(View.VISIBLE);
        } else {
            holder.tvPrice.setVisibility(View.GONE);
        }
        
        // Load image
        String imageUrl = card.getBaseImageUrl();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = BASE_URL + imageUrl;
        }
        
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.img);

        // Hiển thị nút Resell cho thẻ có thể bán lại:
        // - SOLD: Thẻ đã mua, có thể bán lại
        // - REJECTED: Thẻ bị từ chối, có thể sửa và bán lại
        // Không cho phép resell:
        // - PENDING: Đang chờ admin duyệt
        // - APPROVED: Đang được rao bán trên marketplace
        boolean canResell = "SOLD".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status);
        holder.btnResell.setVisibility(canResell ? View.VISIBLE : View.GONE);
        holder.btnResell.setOnClickListener(v -> {
            if (listener != null) listener.onResell(card);
        });
    }
    
    private int getRarityBackground(String rarity) {
        if (rarity == null) return R.drawable.status_badge_approved;
        switch (rarity.toUpperCase()) {
            case "LEGENDARY": return R.drawable.status_badge_approved;
            case "RARE": return R.drawable.status_badge_pending;
            default: return R.drawable.status_badge_approved;
        }
    }
    
    private int getRarityColor(String rarity) {
        if (rarity == null) return R.color.approved_green;
        switch (rarity.toUpperCase()) {
            case "LEGENDARY": return R.color.approved_green;
            case "RARE": return R.color.pending_orange;
            default: return R.color.text_secondary;
        }
    }
    
    private int getStatusBackground(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return R.drawable.status_badge_pending;
            case "APPROVED": return R.drawable.status_badge_approved;
            case "REJECTED": return R.drawable.status_badge_rejected;
            case "SOLD": return R.drawable.status_badge_approved;
            default: return R.drawable.status_badge_approved;
        }
    }
    
    private int getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return R.color.pending_orange;
            case "APPROVED": return R.color.approved_green;
            case "REJECTED": return R.color.rejected_red;
            case "SOLD": return R.color.bright_green;
            default: return R.color.text_secondary;
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public interface ResellListener {
        void onResell(MyOwnedCard card);
    }

    private ResellListener listener;
    public void setResellListener(ResellListener l) { this.listener = l; }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvRarity, tvTeam, tvStatus, tvPrice;
        MaterialButton btnResell;
        
        InventoryViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.imgThumb);
            tvName = v.findViewById(R.id.tvName);
            tvRarity = v.findViewById(R.id.tvRarity);
            tvTeam = v.findViewById(R.id.tvTeam);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvPrice = v.findViewById(R.id.tvPrice);
            btnResell = v.findViewById(R.id.btnResell);
        }
    }
    
    @Override
    public void onViewRecycled(@NonNull InventoryViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.btnResell != null) holder.btnResell.setOnClickListener(null);
    }
}
