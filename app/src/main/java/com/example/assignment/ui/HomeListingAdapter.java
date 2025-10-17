package com.example.assignment.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment.R;
import com.example.assignment.data.model.HomeListing;

import java.util.List;

public class HomeListingAdapter extends RecyclerView.Adapter<HomeListingAdapter.VH> {
    private List<HomeListing> items;

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
        holder.title.setText(l.getCardName() != null ? l.getCardName() : "Listing " + l.getListingId());
        holder.price.setText(String.format("%.2f", l.getPrice()));
        holder.owner.setText(l.getOwnerName() != null ? "Owner: " + l.getOwnerName() : "Owner: -");
        holder.itemView.setOnClickListener(v -> {
            // open CardDetailFragment
            androidx.fragment.app.FragmentActivity act = (androidx.fragment.app.FragmentActivity) v.getContext();
            act.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, com.example.assignment.ui.CardDetailFragment.newInstance(l.getCardId()))
                    .addToBackStack(null)
                    .commit();
        });
        // Ẩn nút mua nếu là thẻ của mình
        com.example.assignment.utils.SessionManager session = new com.example.assignment.utils.SessionManager(holder.itemView.getContext());
        String myUsername = session.fetchUsername();
        boolean isMine = l.getOwnerName() != null && l.getOwnerName().equalsIgnoreCase(myUsername);
        if (holder.btnBuy != null) {
            holder.btnBuy.setVisibility(isMine ? View.GONE : View.VISIBLE);
            holder.btnBuy.setOnClickListener(v -> {
                if (!isMine) {
                    android.content.Context ctx = v.getContext();
                    String name = l.getCardName() != null ? l.getCardName() : ("Listing " + l.getListingId());
                    android.widget.Toast.makeText(ctx, "Buy: " + name + " for " + String.format("%.2f", l.getPrice()), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, price, owner;
        android.widget.Button btnBuy;

        public VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            price = itemView.findViewById(R.id.tvPrice);
            owner = itemView.findViewById(R.id.tvOwner);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        holder.itemView.setOnClickListener(null);
        if (holder.btnBuy != null) holder.btnBuy.setOnClickListener(null);
    }
}
