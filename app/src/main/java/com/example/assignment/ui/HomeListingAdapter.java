package com.example.assignment.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.model.HomeListing;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HomeListingAdapter extends RecyclerView.Adapter<HomeListingAdapter.VH> {
    private List<HomeListing> items;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public HomeListingAdapter(List<HomeListing> items) {
        this.items = items;
    }

    public void setItems(List<HomeListing> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        HomeListing l = items.get(position);

        holder.tvName.setText(l.getCardName());
        
        // Rarity with uppercase
        String rarity = l.getRarity();
        if (rarity != null) {
            holder.tvRarity.setText(rarity.toUpperCase());
        } else {
            holder.tvRarity.setText("COMMON");
        }
        
        // Owner info
        String ownerName = l.getOwnerName();
        if (ownerName != null && !ownerName.isEmpty()) {
            holder.tvOwner.setText("Owner: " + ownerName);
            holder.tvOwner.setVisibility(View.VISIBLE);
        } else {
            holder.tvOwner.setVisibility(View.GONE);
        }
        
        // Price formatting
        holder.tvPrice.setText(String.format("VND %,.0f", l.getPrice()));

        // Image loading
        String imageUrl = l.getImage();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = BASE_URL + imageUrl;
        }

        Log.d("HomeListingAdapter", "Loading image from URL: " + imageUrl);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgThumb);

        View.OnClickListener listener = v -> openCardDetail(v.getContext(), l.getCardId());
        holder.itemView.setOnClickListener(listener);
        holder.btnView.setOnClickListener(listener);
    }

    private void openCardDetail(Context context, Long cardId) {
        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, CardDetailFragment.newInstance(cardId))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvRarity, tvOwner, tvPrice;
        MaterialButton btnView;

        public VH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvRarity = itemView.findViewById(R.id.tvRarity);
            tvOwner = itemView.findViewById(R.id.tvOwner);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        holder.itemView.setOnClickListener(null);
        if (holder.btnView != null) holder.btnView.setOnClickListener(null);
    }
}
