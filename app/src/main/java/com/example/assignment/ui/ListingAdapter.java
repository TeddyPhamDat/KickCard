package com.example.assignment.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment.R;
import com.example.assignment.data.model.Listing;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.VH> {
    private List<Listing> items;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public ListingAdapter(List<Listing> items) {
        this.items = items;
    }

    public void setItems(List<Listing> items) {
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
        Listing l = items.get(position);
        holder.tvName.setText(l.getTitle());
        holder.tvRarity.setText(l.getRarity());
        // format price to 2 decimals
        holder.tvPrice.setText(String.format("%.2f", l.getPrice()));

        String imageUrl = l.getImage();
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = BASE_URL + imageUrl;
        }

        Log.d("ListingAdapter", "Loading image from URL: " + imageUrl);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgThumb);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvRarity, tvPrice;

        public VH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvRarity = itemView.findViewById(R.id.tvRarity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
