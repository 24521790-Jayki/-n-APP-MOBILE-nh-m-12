package com.example.group12.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.R;

import java.util.ArrayList;
import java.util.List;

public class CommunityBookAdapter extends RecyclerView.Adapter<CommunityBookAdapter.ViewHolder> {

    private final List<CommunityBookItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CommunityBookItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CommunityBookItem> newItems) {
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
                .inflate(R.layout.item_community_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityBookItem item = items.get(position);
        holder.tvBookTitle.setText(item.getTitle());
        holder.tvBookAuthor.setText(item.getAuthor());
        holder.tvContributor.setText("Đóng góp bởi: " + item.getContributor());
        holder.tvBookCoverInitial.setText(item.getInitial());

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
        TextView tvBookTitle, tvBookAuthor, tvContributor, tvBookCoverInitial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvContributor = itemView.findViewById(R.id.tvContributor);
            tvBookCoverInitial = itemView.findViewById(R.id.tvBookCoverInitial);
        }
    }
}
