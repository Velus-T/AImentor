package com.example.historymodule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.historymodule.R;
import com.example.historymodule.databases.HistoryHistoryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Role: Adapter for History, responsible for receiving model data and binding it to RecyclerView/ListView items.
 * Purpose: The Adapter separates list data from presentation logic, making the UI easier to update and reuse.
 */
public final class HistoryHistoryAdapter
        extends RecyclerView.Adapter<HistoryHistoryAdapter.ViewHolder> {

    public interface Listener {
        void onClick(HistoryHistoryItem item);
        void onLongClick(HistoryHistoryItem item);
    }

    private final List<HistoryHistoryItem> historyList;
    private final Listener listener;
    private final SimpleDateFormat formatter =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    /*
     * Responsibility: Initializes the `HistoryHistoryAdapter` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public HistoryHistoryAdapter(List<HistoryHistoryItem> historyList) {
        this(historyList, null);
    }

    public HistoryHistoryAdapter(List<HistoryHistoryItem> historyList, Listener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    /*
     * Responsibility: Handles the Android callback or event `onCreateViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item_history_row, parent, false);
        return new ViewHolder(view);
    }

    /*
     * Responsibility: Handles the Android callback or event `onBindViewHolder` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryHistoryItem item = historyList.get(position);
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());
        holder.time.setText(formatTimestamp(item.getTimestamp()));
        holder.itemView.setClickable(listener != null);
        holder.itemView.setFocusable(listener != null);
        holder.itemView.setOnClickListener(listener == null ? null : v -> listener.onClick(item));
        holder.itemView.setOnLongClickListener(listener == null ? null : v -> {
            listener.onLongClick(item);
            return true;
        });
    }

    /*
     * Responsibility: Converts or normalizes data in `format timestamp` into the required format.
     * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
     */
    private String formatTimestamp(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        try {
            long millis = Long.parseLong(value.trim());
            return millis <= 0L ? "" : formatter.format(new Date(millis));
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    /*
     * Responsibility: Reads or loads data for `get item count` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView content;
        final TextView time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.history_title);
            content = itemView.findViewById(R.id.history_content);
            time = itemView.findViewById(R.id.history_time);
        }
    }
}
