package com.example.assignment.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.model.MyOwnedCard;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private List<MyOwnedCard> items = new ArrayList<>();

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
        holder.tvName.setText(card.getName());
        holder.tvStatus.setText(card.getStatus());
        // Load image if available (optional)
        // Glide.with(holder.img.getContext()).load(card.getBaseImageUrl()).into(holder.img);

        boolean canResell = ("SOLD".equalsIgnoreCase(card.getStatus()) || "APPROVED".equalsIgnoreCase(card.getStatus()));
        holder.btnResell.setVisibility(canResell ? View.VISIBLE : View.GONE);
        holder.btnResell.setOnClickListener(v -> {
            if (listener != null) listener.onResell(card);
        });
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
        TextView tvName, tvStatus;
        ImageButton btnResell;
        InventoryViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.imgThumb);
            tvName = v.findViewById(R.id.tvName);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnResell = v.findViewById(R.id.btnResell);
        }
    }
}
