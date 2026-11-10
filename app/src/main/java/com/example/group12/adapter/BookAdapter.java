
package com.example.group12.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.R;
import com.example.group12.api.Book;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<Book> bookList = new ArrayList<>();

    public void setBooks(List<Book> books) {
        bookList.clear();

        if (books != null) {
            bookList.addAll(books);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);

        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull BookViewHolder holder,
            int position
    ) {
        Book book = bookList.get(position);

        holder.txtBookTitle.setText(book.getTitle());
        holder.txtBookAuthor.setText(book.getAuthor());

        String category = book.getCategoryName();

        if (category == null || category.trim().isEmpty()) {
            category = "Chưa phân loại";
        }

        holder.txtBookCategory.setText(category);
        holder.txtBookCoverTitle.setText(book.getTitle());

        holder.txtBookStatus.setText("Có sẵn");
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        TextView txtBookTitle;
        TextView txtBookAuthor;
        TextView txtBookCategory;
        TextView txtBookStatus;
        TextView txtBookCoverTitle;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBookTitle = itemView.findViewById(R.id.txtBookTitle);
            txtBookAuthor = itemView.findViewById(R.id.txtBookAuthor);
            txtBookCategory = itemView.findViewById(R.id.txtBookCategory);
            txtBookStatus = itemView.findViewById(R.id.txtBookStatus);
            txtBookCoverTitle = itemView.findViewById(R.id.txtBookCoverTitle);
        }
    }
}
