package com.example.group12.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.R;

import java.util.ArrayList;
import java.util.List;

public class MyUploadedBookAdapter extends RecyclerView.Adapter<MyUploadedBookAdapter.ViewHolder> {

    private final List<MyUploadedBookItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MyUploadedBookItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<MyUploadedBookItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_uploaded_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyUploadedBookItem item = items.get(position);
        holder.tvBookTitle.setText(item.getTitle());
        holder.tvBookAuthor.setText("Tác giả: " + item.getAuthor());
        holder.tvUploadDate.setText("Ngày tải lên: " + item.getUploadDate());
        holder.tvBookCoverInitial.setText(item.getInitial());

        holder.tvBookStatusBadge.setText(item.getStatus());
        if (item.getStatus().equals("Đã duyệt")) {
            holder.tvBookStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
            holder.tvBookStatusBadge.setTextColor(Color.parseColor("#16A34A"));
        } else {
            holder.tvBookStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
            holder.tvBookStatusBadge.setTextColor(Color.parseColor("#D97706"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvBookAuthor, tvUploadDate, tvBookStatusBadge, tvBookCoverInitial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvUploadDate = itemView.findViewById(R.id.tvUploadDate);
            tvBookStatusBadge = itemView.findViewById(R.id.tvBookStatusBadge);
            tvBookCoverInitial = itemView.findViewById(R.id.tvBookCoverInitial);
        }
    }
}
