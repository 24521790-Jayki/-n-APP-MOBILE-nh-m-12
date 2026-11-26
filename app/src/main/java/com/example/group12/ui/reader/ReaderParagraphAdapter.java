package com.example.group12.ui.reader;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.R;
import com.example.group12.model.Paragraph;
import com.example.group12.model.Sentence;

import java.util.ArrayList;
import java.util.List;

// Adapter hien thi danh sach cac doan van (Paragraph) trong RecyclerView
// Ho tro highlight cau dang duoc TTS doc va tu dong thay doi Mau chu theo Theme (Light, Dark, Sepia)
public class ReaderParagraphAdapter
        extends RecyclerView.Adapter<ReaderParagraphAdapter.ParagraphViewHolder> {

    private List<Paragraph> paragraphs = new ArrayList<>();
    private int highlightParaIdx = -1;
    private int highlightSentIdx = -1;

    private int textColor = Color.parseColor("#1F2937");
    private int highlightBgColor = Color.parseColor("#FDE68A");

    // Listener khi nguoi dung nhan vao doan van
    private OnParagraphClickListener clickListener;

    public interface OnParagraphClickListener {
        void onParagraphClick(int paragraphIndex);
    }

    public void setOnParagraphClickListener(OnParagraphClickListener listener) {
        this.clickListener = listener;
    }

    // Cap nhat theme mau chu & mau highlight
    public void setTheme(int textColor, int highlightBgColor) {
        this.textColor = textColor;
        this.highlightBgColor = highlightBgColor;
        notifyDataSetChanged();
    }

    // Cap nhat danh sach doan van
    public void setParagraphs(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs != null ? paragraphs : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Cap nhat vi tri highlight (khi TTS dang doc)
    public void setHighlight(int paraIdx, int sentIdx) {
        int oldParaIdx = this.highlightParaIdx;
        this.highlightParaIdx = paraIdx;
        this.highlightSentIdx = sentIdx;

        // Chi cap nhat cac item thay doi
        if (oldParaIdx >= 0 && oldParaIdx < paragraphs.size()) {
            notifyItemChanged(oldParaIdx);
        }
        if (paraIdx >= 0 && paraIdx < paragraphs.size()) {
            notifyItemChanged(paraIdx);
        }
    }

    private int dpToPx(View view, int dp) {
        return (int) (dp * view.getContext().getResources().getDisplayMetrics().density);
    }

    @NonNull
    @Override
    public ParagraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paragraph, parent, false);
        return new ParagraphViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParagraphViewHolder holder, int position) {
        Paragraph para = paragraphs.get(position);

        holder.txtParagraph.setTextColor(textColor);

        String type = para.getParagraphType();
        if ("heading".equals(type)) {
            holder.txtParagraph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            holder.txtParagraph.setTypeface(null, Typeface.BOLD);
            holder.txtParagraph.setGravity(Gravity.CENTER);
            holder.txtParagraph.setPadding(dpToPx(holder.itemView, 16), dpToPx(holder.itemView, 16), dpToPx(holder.itemView, 16), dpToPx(holder.itemView, 16));
        } else if ("center".equals(type)) {
            holder.txtParagraph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            holder.txtParagraph.setTypeface(null, Typeface.ITALIC);
            holder.txtParagraph.setGravity(Gravity.CENTER);
            holder.txtParagraph.setPadding(dpToPx(holder.itemView, 20), dpToPx(holder.itemView, 8), dpToPx(holder.itemView, 20), dpToPx(holder.itemView, 8));
        } else {
            holder.txtParagraph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            holder.txtParagraph.setTypeface(null, Typeface.NORMAL);
            holder.txtParagraph.setGravity(Gravity.START | Gravity.TOP);
            holder.txtParagraph.setPadding(dpToPx(holder.itemView, 20), dpToPx(holder.itemView, 6), dpToPx(holder.itemView, 20), dpToPx(holder.itemView, 10));
        }

        if (position == highlightParaIdx && highlightSentIdx >= 0) {
            // Highlight cau dang doc
            SpannableString spannable = new SpannableString(para.getFullText());
            List<Sentence> sentences = para.getSentences();

            if (highlightSentIdx < sentences.size()) {
                Sentence sent = sentences.get(highlightSentIdx);
                int start = Math.max(0, sent.getCharStart());
                int end = Math.min(para.getFullText().length(), sent.getCharEnd());

                spannable.setSpan(
                        new BackgroundColorSpan(highlightBgColor),
                        start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            holder.txtParagraph.setText(spannable);
        } else {
            holder.txtParagraph.setText(para.getFullText());
        }

        // Xu ly su kien click de kich hoat TTS tu doan van nay
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onParagraphClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return paragraphs.size();
    }

    static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        TextView txtParagraph;

        ParagraphViewHolder(@NonNull View itemView) {
            super(itemView);
            txtParagraph = itemView.findViewById(R.id.txtParagraph);
        }
    }
}
