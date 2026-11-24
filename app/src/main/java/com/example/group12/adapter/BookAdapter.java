package com.example.group12.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.R;
import com.example.group12.api.Book;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public static final int VIEW_TYPE_GRID = 0;
    public static final int VIEW_TYPE_LIST = 1;

    public static final int SORT_NEWEST = 0;
    public static final int SORT_OLDEST = 1;
    public static final int SORT_TITLE_AZ = 2;
    public static final int SORT_AUTHOR_AZ = 3;
    public static final int SORT_RECENT_READ = 4;

    // Danh sach sach goc
    private final List<Book> allBooks = new ArrayList<>();

    // Danh sach sach sau khi loc va sap xep
    private final List<Book> bookList = new ArrayList<>();

    private int currentDisplayMode = VIEW_TYPE_GRID;
    private boolean showAudioBadge = true;
    private int currentSortType = SORT_NEWEST;
    private String currentKeyword = "";

    // Callback khi nhan vao sach
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private OnBookClickListener listener;

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setDisplayMode(int displayMode) {
        if (this.currentDisplayMode != displayMode) {
            this.currentDisplayMode = displayMode;
            notifyDataSetChanged();
        }
    }

    public int getDisplayMode() {
        return currentDisplayMode;
    }

    public void setShowAudioBadge(boolean showAudioBadge) {
        this.showAudioBadge = showAudioBadge;
        notifyDataSetChanged();
    }

    // Cap nhat danh sach sach
    public void setBooks(List<Book> books) {
        allBooks.clear();

        if (books != null) {
            allBooks.addAll(books);
        }

        applyFilterAndSort();
    }

    // Tim kiem sach theo ten sach va tac gia
    public void filterBooks(String query) {
        this.currentKeyword = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        applyFilterAndSort();
    }

    // Sap xep sach
    public void sortBooks(int sortType) {
        this.currentSortType = sortType;
        applyFilterAndSort();
    }

    public int getCurrentSortType() {
        return currentSortType;
    }

    private void applyFilterAndSort() {
        bookList.clear();

        for (Book book : allBooks) {
            String title = book.getTitle() == null ? "" : book.getTitle().toLowerCase(Locale.ROOT);
            String author = book.getAuthor() == null ? "" : book.getAuthor().toLowerCase(Locale.ROOT);
            String category = book.getCategoryName() == null ? "" : book.getCategoryName().toLowerCase(Locale.ROOT);

            if (currentKeyword.isEmpty()
                    || title.contains(currentKeyword)
                    || author.contains(currentKeyword)
                    || category.contains(currentKeyword)) {
                bookList.add(book);
            }
        }

        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.PRIMARY);

        switch (currentSortType) {
            case SORT_OLDEST:
                Collections.sort(bookList, (b1, b2) -> Integer.compare(b1.getBookID(), b2.getBookID()));
                break;
            case SORT_TITLE_AZ:
                Collections.sort(bookList, (b1, b2) -> {
                    String t1 = b1.getTitle() == null ? "" : b1.getTitle();
                    String t2 = b2.getTitle() == null ? "" : b2.getTitle();
                    return collator.compare(t1, t2);
                });
                break;
            case SORT_AUTHOR_AZ:
                Collections.sort(bookList, (b1, b2) -> {
                    String a1 = b1.getAuthor() == null ? "" : b1.getAuthor();
                    String a2 = b2.getAuthor() == null ? "" : b2.getAuthor();
                    return collator.compare(a1, a2);
                });
                break;
            case SORT_RECENT_READ:
            case SORT_NEWEST:
            default:
                Collections.sort(bookList, (b1, b2) -> Integer.compare(b2.getBookID(), b1.getBookID()));
                break;
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return currentDisplayMode;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        int layoutId = viewType == VIEW_TYPE_LIST ? R.layout.item_book_list : R.layout.item_book;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);

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
                book.getAuthor() == null || book.getAuthor().trim().isEmpty()
                        ? "Khuyết danh"
                        : book.getAuthor()
        );

        holder.txtBookCategory.setText(
                book.getCategoryName() == null || book.getCategoryName().trim().isEmpty()
                        ? "Chưa phân loại"
                        : book.getCategoryName()
        );

        if (holder.txtBookCoverTitle != null) {
            holder.txtBookCoverTitle.setText(book.getTitle());
        }

        if (holder.txtBookStatus != null) {
            holder.txtBookStatus.setText("Có sẵn");
        }

        if (holder.txtAudioBadge != null) {
            holder.txtAudioBadge.setVisibility(showAudioBadge ? View.VISIBLE : View.GONE);
        }

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
        TextView txtAudioBadge;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBookTitle = itemView.findViewById(R.id.txtBookTitle);
            txtBookAuthor = itemView.findViewById(R.id.txtBookAuthor);
            txtBookCategory = itemView.findViewById(R.id.txtBookCategory);
            txtBookCoverTitle = itemView.findViewById(R.id.txtBookCoverTitle);
            txtBookStatus = itemView.findViewById(R.id.txtBookStatus);
            txtAudioBadge = itemView.findViewById(R.id.txtAudioBadge);
        }
    }
}
