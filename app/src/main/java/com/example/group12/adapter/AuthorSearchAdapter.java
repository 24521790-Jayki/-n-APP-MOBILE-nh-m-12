package com.example.group12.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AuthorSearchAdapter extends RecyclerView.Adapter<AuthorSearchAdapter.ViewHolder> {

    private final List<AuthorSearchItem> items = new ArrayList<>();
    private OnAuthorClickListener listener;

    public interface OnAuthorClickListener {
        void onAuthorClick(AuthorSearchItem author);
    }

    public void setOnAuthorClickListener(OnAuthorClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AuthorSearchItem> newItems) {
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
                .inflate(R.layout.item_search_author, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuthorSearchItem author = items.get(position);
        holder.tvAuthorName.setText(author.getAuthorName());
        holder.tvBookCount.setText(author.getBookCount());

        holder.btnViewAuthorBooks.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAuthorClick(author);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAuthorClick(author);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthorName, tvBookCount;
        MaterialButton btnViewAuthorBooks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvBookCount = itemView.findViewById(R.id.tvBookCount);
            btnViewAuthorBooks = itemView.findViewById(R.id.btnViewAuthorBooks);
        }
    }
}
