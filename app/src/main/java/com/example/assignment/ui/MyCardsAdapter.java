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
        vh.name.setText(c.getName() == null ? "Unnamed" : c.getName());
        vh.status.setText(c.getStatus() == null ? "UNKNOWN" : c.getStatus());
        if (c.getBaseImageUrl() != null && !c.getBaseImageUrl().isEmpty()) {
            Glide.with(vh.img.getContext()).load(c.getBaseImageUrl()).into(vh.img);
        } else {
            vh.img.setImageResource(R.mipmap.ic_launcher);
        }

        boolean isPending = "PENDING".equalsIgnoreCase(c.getStatus());
        vh.btnEdit.setVisibility(isPending ? View.VISIBLE : View.GONE);
        vh.btnDelete.setVisibility(isPending ? View.VISIBLE : View.GONE);

        // Hiển thị nút Resell cho thẻ SOLD hoặc APPROVED (bất kể có giá hay chưa)
        boolean canResell = ("SOLD".equalsIgnoreCase(c.getStatus()) || "APPROVED".equalsIgnoreCase(c.getStatus()));
        vh.btnResell.setVisibility(canResell ? View.VISIBLE : View.GONE);

        vh.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(c); });
        vh.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(c); });
        vh.btnResell.setOnClickListener(v -> {
            if (listener instanceof MyCardsAdapter.ResellListener) {
                ((ResellListener) listener).onResell(c);
            }
        });

        // Sự kiện click vào toàn bộ item để xem chi tiết
        vh.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(c);
        });
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, status;
        ImageButton btnEdit, btnDelete, btnResell;
        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgThumb);
            name = itemView.findViewById(R.id.tvName);
            status = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnResell = itemView.findViewById(R.id.btnResell);
        }
    }
}
