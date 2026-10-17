package com.example.historymodule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.historymodule.R;
import com.example.historymodule.databases.HistoryHistoryItem;
import java.util.List;

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
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(com.example.mentor.ui.R.layout.mentor_history_row, parent, false);
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
    holder.time.setText(
        com.example.mentor.ui.TimeLabels.dateTime(
            holder.itemView.getContext(), item.getTimestamp()));
    String group = groupLabel(item.getTimestamp(), holder.itemView.getContext());
    boolean showGroup =
        listener != null
            && (position == 0
                || !group.equals(
                    groupLabel(
                        historyList.get(position - 1).getTimestamp(),
                        holder.itemView.getContext())));
    holder.group.setText(group);
    holder.group.setVisibility(showGroup ? View.VISIBLE : View.GONE);
    holder.options.setVisibility(listener == null ? View.GONE : View.VISIBLE);
    holder.options.setOnClickListener(
        v -> {
          android.widget.PopupMenu menu = new android.widget.PopupMenu(v.getContext(), v);
          menu.getMenu().add(0, 1, 0, R.string.history_open_conversation);
          menu.getMenu().add(0, 2, 1, R.string.history_delete_conversation);
          menu.setOnMenuItemClickListener(
              action -> {
                if (action.getItemId() == 1) listener.onClick(item);
                else listener.onLongClick(item);
                return true;
              });
          menu.show();
        });
    holder.body.setClickable(listener != null);
    holder.body.setFocusable(listener != null);
    holder.body.setOnClickListener(listener == null ? null : v -> listener.onClick(item));
    holder.body.setOnLongClickListener(
        listener == null
            ? null
            : v -> {
              listener.onLongClick(item);
              return true;
            });
  }

  /*
   * Responsibility: Converts or normalizes data in `format timestamp` into the required format.
   * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
   */
  private String groupLabel(String raw, android.content.Context context) {
    return com.example.mentor.ui.HistoryLabels.group(context, raw);
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
    final TextView group;
    final View options;
    final View body;
    final TextView title;
    final TextView content;
    final TextView time;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      body = itemView.findViewById(com.example.mentor.ui.R.id.mentor_history_row_body);
      group = itemView.findViewById(com.example.mentor.ui.R.id.mentor_history_group);
      options = itemView.findViewById(com.example.mentor.ui.R.id.mentor_history_row_options);
      title = itemView.findViewById(com.example.mentor.ui.R.id.mentor_history_row_title);
      content = itemView.findViewById(com.example.mentor.ui.R.id.mentor_history_row_preview);
      time = itemView.findViewById(com.example.mentor.ui.R.id.mentor_history_row_time);
    }
  }
}
