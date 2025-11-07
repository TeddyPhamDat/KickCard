package com.example.assignment.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.assignment.R;
import com.example.assignment.data.model.Card;
import com.example.assignment.data.model.User;
// listings are not used in admin adapter anymore

import java.util.ArrayList;
import java.util.List;

public class AdminCardAdapter extends RecyclerView.Adapter<AdminCardAdapter.VH> {

    public interface Listener {
        void onApprove(Object item); // Card
        void onReject(Object item);
        void onEdit(Object item);
        void onDelete(Object item);
    }

    private List<Object> items = new ArrayList<>();
    private Listener listener;

    public AdminCardAdapter(Listener listener) { this.listener = listener; }

    public void setItems(List<Object> items) { this.items = items == null ? new ArrayList<>() : items; notifyDataSetChanged(); }

    public int removeItem(Object item) {
        if (items == null) return -1;
        int idx = items.indexOf(item);
        if (idx >= 0) {
            items.remove(idx);
            notifyItemRemoved(idx);
            return idx;
        }
        return -1;
    }

    public void insertItem(Object item, int index) {
        if (items == null) items = new ArrayList<>();
        if (index < 0 || index > items.size()) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        } else {
            items.add(index, item);
            notifyItemInserted(index);
        }
    }

    public List<Object> getItems() { return items; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Object o = items.get(position);
        String info = "";
        String status = "PENDING";
        int statusBgResource = R.drawable.status_badge_pending;
        
        if (o instanceof Card) {
            Card c = (Card) o;
            // Show all fields for admin pending card info (excluding image URL)
            info = "ID: " + c.getId()
                    + "\nName: " + c.getName()
                    + "\nTeam: " + c.getTeam()
                    + "\nRarity: " + c.getRarity()
                    + "\nDescription: " + c.getDescription()
                    + "\nPrice: " + (c.getPrice() == null ? "N/A" : c.getPrice())
                    + "\nOwner ID: " + c.getOwnerId()
                    + "\nStatus: " + c.getStatus()
                    + (c.getRejectionReason() != null && !c.getRejectionReason().isEmpty() ? "\nRejection Reason: " + c.getRejectionReason() : "");
            status = c.getStatus();
            
            // Set status badge background based on status
            if (status != null) {
                if (status.equalsIgnoreCase("APPROVED")) {
                    statusBgResource = R.drawable.status_badge_approved;
                } else if (status.equalsIgnoreCase("REJECTED")) {
                    statusBgResource = R.drawable.status_badge_rejected;
                } else {
                    statusBgResource = R.drawable.status_badge_pending;
                }
            }
            
            // Load card image using Glide
            holder.cardImageContainer.setVisibility(View.VISIBLE);
            if (c.getBaseImageUrl() != null && !c.getBaseImageUrl().isEmpty()) {
                String imageUrl = c.getBaseImageUrl();
                // If URL is relative, prepend base URL
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "http://10.0.2.2:8080" + (imageUrl.startsWith("/") ? "" : "/") + imageUrl;
                }
                
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(holder.ivCardImage);
            } else {
                // No image available
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.ic_image_placeholder)
                        .into(holder.ivCardImage);
            }
            
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else if (o instanceof User) {
            User u = (User) o;
            // Show all user fields for admin
            info = "ID: " + u.getId()
                    + "\nUsername: " + u.getUsername()
                    + "\nEmail: " + u.getEmail()
                    + "\nFullname: " + u.getFullname()
                    + "\nPhone: " + u.getPhone()
                    + "\nAddress: " + u.getAddress()
                    + "\nRole: " + u.getRole();
            status = u.getRole();
            
            // For users, use admin accent badge
            statusBgResource = R.drawable.status_badge_approved;
            
            // Hide image container for users
            holder.cardImageContainer.setVisibility(View.GONE);
            
            // for users we only allow edit/delete (no approve/reject)
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.cardImageContainer.setVisibility(View.GONE);
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
        holder.tvInfo.setText(info);
        holder.tvStatus.setText(status);
        holder.tvStatus.setBackgroundResource(statusBgResource);

        holder.btnApprove.setOnClickListener(v -> { if (listener != null) listener.onApprove(o); });
        holder.btnReject.setOnClickListener(v -> { if (listener != null) listener.onReject(o); });
        holder.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(o); });
        holder.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(o); });
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvInfo, tvStatus;
        ImageView ivCardImage;
        View cardImageContainer;
        android.widget.ImageButton btnApprove, btnReject, btnEdit, btnDelete;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivCardImage = itemView.findViewById(R.id.ivCardImage);
            cardImageContainer = itemView.findViewById(R.id.cardImageContainer);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
