package com.example.group12.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.R;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final List<SearchResultItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SearchResultItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SearchResultItem> newItems) {
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
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultItem item = items.get(position);
        holder.tvBookTitle.setText(item.getTitle());
        holder.tvBookAuthor.setText(item.getAuthor());
        holder.tvBookTag.setText(item.getTag());
        holder.tvBookRating.setText(item.getRating());
        holder.tvBookReviews.setText(item.getReviews());
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
        TextView tvBookTitle, tvBookAuthor, tvBookTag, tvBookRating, tvBookReviews, tvBookCoverInitial;
        ImageView btnBookmark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookTag = itemView.findViewById(R.id.tvBookTag);
            tvBookRating = itemView.findViewById(R.id.tvBookRating);
            tvBookReviews = itemView.findViewById(R.id.tvBookReviews);
            tvBookCoverInitial = itemView.findViewById(R.id.tvBookCoverInitial);
            btnBookmark = itemView.findViewById(R.id.btnBookmark);
        }
    }
}
