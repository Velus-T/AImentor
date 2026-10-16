package com.example.mathmodule.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathmodule.MathMessage;
import com.example.mathmodule.R;

import java.util.List;

/*
 * Role: Adapter for Math, responsible for receiving model data and binding it to RecyclerView/ListView items.
 * Purpose: The Adapter separates list data from presentation logic, making the UI easier to update and reuse.
 */
public class MathChatAdapter extends RecyclerView.Adapter<MathChatAdapter.MessageViewHolder> {

    private List<MathMessage> messageList;

    public MathChatAdapter(List<MathMessage> messageList) {
        this.messageList = messageList;
    }

    /*
     * Responsibility: Handles the Android callback or event `onCreateViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_math_message, parent, false);
        return new MessageViewHolder(view);
    }

    /*
     * Responsibility: Handles the Android callback or event `onBindViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MathMessage message = messageList.get(position);

        // 1. Phân biệt tin nhắn User và AI để căn lề và đổi màu
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.cardView.getLayoutParams();
        if (message.isUser()) {
            params.gravity = Gravity.END;
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        } else {
            params.gravity = Gravity.START;
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        holder.cardView.setLayoutParams(params);

        // 2. Cấu hình WebView
        WebSettings webSettings = holder.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // [SỬA LỖI Ở ĐÂY] - GIỮ NGUYÊN VĂN BẢN GỐC, KHÔNG DÙNG .replace("\n", "<br>")
        String safeText = message.getContent();

        // Thêm "white-space: pre-wrap;" vào thẻ <style> để xử lý xuống dòng tự nhiên mà không làm gãy KaTeX
        String htmlContent = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css\">" +
                "<script defer src=\"https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js\"></script>" +
                "<script defer src=\"https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js\" " +
                "onload=\"renderMathInElement(document.body, {delimiters: [{left: '$$', right: '$$', display: true},{left: '$', right: '$', display: false}], throwOnError: false});\"></script>" +
                "<style>body { font-family: sans-serif; font-size: 16px; color: #000000; padding: 8px; margin: 0; word-wrap: break-word; white-space: pre-wrap; }</style>" +
                "</head><body>" + safeText + "</body></html>";

        // Tải nội dung lên WebView
        holder.webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    /*
     * Responsibility: Reads or loads data for `get item count` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        WebView webView;
        CardView cardView;

        /*
         * Responsibility: Performs the `message view holder` responsibility of `MathChatAdapter` and returns or updates the result for the caller.
         * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
         */
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            webView = itemView.findViewById(R.id.math_webview_message);
            cardView = itemView.findViewById(R.id.math_card_message);
        }
    }
}