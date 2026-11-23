
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
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    // Danh sach sach goc
    private final List<Book> allBooks = new ArrayList<>();

    // Danh sach sach sau khi tim kiem
    private final List<Book> bookList = new ArrayList<>();

    // Callback khi nhan vao sach
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private OnBookClickListener listener;

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    // Cap nhat danh sach sach
    public void setBooks(List<Book> books) {
        allBooks.clear();

        if (books != null) {
            allBooks.addAll(books);
        }

        filterBooks("");
    }

    // Tim kiem sach theo ten sach va tac gia
    public void filterBooks(String query) {
        bookList.clear();

        String keyword = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        for (Book book : allBooks) {
            String title = book.getTitle() == null
                    ? ""
                    : book.getTitle().toLowerCase(Locale.ROOT);

            String author = book.getAuthor() == null
                    ? ""
                    : book.getAuthor().toLowerCase(Locale.ROOT);

            if (title.contains(keyword) || author.contains(keyword)) {
                bookList.add(book);
            }
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

        holder.txtBookAuthor.setText(
                book.getAuthor() == null
                        ? ""
                        : book.getAuthor()
        );

        holder.txtBookCategory.setText(
                book.getCategoryName() == null
                        ? "Chưa phân loại"
                        : book.getCategoryName()
        );

        holder.txtBookCoverTitle.setText(book.getTitle());

        holder.txtBookStatus.setText("Có sẵn");

        // Su kien nhan vao sach
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        TextView txtBookTitle;
        TextView txtBookAuthor;
        TextView txtBookCategory;
        TextView txtBookCoverTitle;
        TextView txtBookStatus;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBookTitle = itemView.findViewById(R.id.txtBookTitle);
            txtBookAuthor = itemView.findViewById(R.id.txtBookAuthor);
            txtBookCategory = itemView.findViewById(R.id.txtBookCategory);
            txtBookCoverTitle = itemView.findViewById(R.id.txtBookCoverTitle);
            txtBookStatus = itemView.findViewById(R.id.txtBookStatus);
        }
    }
}


