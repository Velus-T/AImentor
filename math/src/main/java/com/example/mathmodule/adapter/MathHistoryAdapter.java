package com.example.mathmodule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathmodule.R;
import com.example.mathmodule.databases.MathHistoryItem;

import java.util.List;

/*
 * Role: Adapter for Math, responsible for receiving model data and binding it to RecyclerView/ListView items.
 * Purpose: The Adapter separates list data from presentation logic, making the UI easier to update and reuse.
 */
public class MathHistoryAdapter extends RecyclerView.Adapter<MathHistoryAdapter.HistoryViewHolder> {

    private List<MathHistoryItem> historyList;

    public MathHistoryAdapter(List<MathHistoryItem> historyList) {
        this.historyList = historyList;
    }

    /*
     * Responsibility: Updates or synchronizes `update data` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    public void updateData(List<MathHistoryItem> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_row, parent, false);
        return new HistoryViewHolder(view);
    }

    /*
     * Responsibility: Handles the Android callback or event `onBindViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        MathHistoryItem item = historyList.get(position);

        // 1. Tách chuỗi thời gian
        String fullDateTime = item.getTimestamp();
        String dateOnly = fullDateTime.contains(" ") ? fullDateTime.split(" ")[0] : fullDateTime;
        String timeOnly = fullDateTime.contains(" ") ? fullDateTime.split(" ")[1] : "";

        // 2. Logic "Chia theo ngày"
        boolean showHeader = false;
        if (position == 0) {
            showHeader = true;
        } else {
            MathHistoryItem previousItem = historyList.get(position - 1);
            String previousDate = previousItem.getTimestamp().contains(" ") ? previousItem.getTimestamp().split(" ")[0] : previousItem.getTimestamp();
            if (!dateOnly.equals(previousDate)) {
                showHeader = true;
            }
        }

        if (showHeader) {
            holder.txtDateHeader.setVisibility(View.VISIBLE);
            holder.txtDateHeader.setText("Ngày " + dateOnly);
        } else {
            holder.txtDateHeader.setVisibility(View.GONE);
        }

        // 3. Đổ dữ liệu Câu hỏi và Thời gian
        holder.txtQuestion.setText(item.getQuestion());
        holder.txtTime.setText(timeOnly + " ▼");

        // --- XỬ LÝ RENDER TOÁN HỌC VỚI WEBVIEW ---
        holder.webviewAnswer.getSettings().setJavaScriptEnabled(true);
        holder.webviewAnswer.setVerticalScrollBarEnabled(false);
        holder.webviewAnswer.setBackgroundColor(0); // Làm trong suốt nền

        // Thay thế ký tự xuống dòng thành thẻ <br> của HTML
        String answerText = item.getAnswer() != null ? item.getAnswer().replace("\n", "<br>") : "";

        // Bọc nội dung bằng HTML và nhúng thư viện MathJax để render LaTeX
        String htmlContent = "<!DOCTYPE html><html><head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.9/MathJax.js?config=TeX-MML-AM_CHTML\"></script>" +
                "<style>body { font-family: Arial, sans-serif; font-size: 15px; color: #555555; margin: 0; padding: 0; line-height: 1.5; word-wrap: break-word; }</style>" +
                "</head><body>" + answerText + "</body></html>";

        holder.webviewAnswer.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

        // 4. Sự kiện click để trượt mở Lời giải
        holder.itemView.setOnClickListener(v -> {
            if (holder.layoutAnswer.getVisibility() == View.GONE) {
                holder.layoutAnswer.setVisibility(View.VISIBLE);
                holder.txtTime.setText(timeOnly + " ▲");
                holder.txtQuestion.setMaxLines(Integer.MAX_VALUE);
            } else {
                holder.layoutAnswer.setVisibility(View.GONE);
                holder.txtTime.setText(timeOnly + " ▼");
                holder.txtQuestion.setMaxLines(1);
            }
        });
    }

    /*
     * Responsibility: Reads or loads data for `get item count` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtDateHeader, txtQuestion, txtTime;
        WebView webviewAnswer; // Đổi sang WebView
        LinearLayout layoutAnswer;

        /*
         * Responsibility: Performs the `history view holder` responsibility of `MathHistoryAdapter` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDateHeader = itemView.findViewById(R.id.txt_date_group_header);
            txtQuestion = itemView.findViewById(R.id.txt_item_question);
            txtTime = itemView.findViewById(R.id.txt_item_time);
            webviewAnswer = itemView.findViewById(R.id.webview_item_answer); // Ánh xạ WebView
            layoutAnswer = itemView.findViewById(R.id.layout_item_answer);
        }
    }
}