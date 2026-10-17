package com.example.modulelaptrinh.adapters;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mentor.ui.*;
import com.example.modulelaptrinh.database.SessionItem;
import java.util.*;

/** Actual session titles grouped by date, with discoverable Open/Delete actions. */
public final class ProgrammingChatHistoryAdapter
    extends RecyclerView.Adapter<ProgrammingChatHistoryAdapter.Holder> {
  public interface OnDeleteClickListener {
    void onDeleteClick(int position, SessionItem session);

    void onItemClick(int position, SessionItem session);
  }

  private List<SessionItem> items;
  private final OnDeleteClickListener listener;

  public ProgrammingChatHistoryAdapter(List<SessionItem> items, OnDeleteClickListener listener) {
    this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    this.listener = listener;
  }

  public void updateData(List<SessionItem> items) {
    this.items = new ArrayList<>(items);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
    return new Holder(
        LayoutInflater.from(parent.getContext())
            .inflate(com.example.mentor.ui.R.layout.mentor_history_row, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    SessionItem item = items.get(position);
    holder.title.setText(item.getName());
    holder.preview.setVisibility(View.GONE);
    holder.time.setText(TimeLabels.dateTime(holder.itemView.getContext(), item.getTimestamp()));
    String group = HistoryLabels.group(holder.itemView.getContext(), item.getTimestamp());
    holder.group.setText(group);
    holder.group.setVisibility(
        position == 0
                || !group.equals(
                    HistoryLabels.group(
                        holder.itemView.getContext(), items.get(position - 1).getTimestamp()))
            ? View.VISIBLE
            : View.GONE);
    holder.body.setOnClickListener(
        v -> {
          if (listener != null) listener.onItemClick(position, item);
        });
    holder.options.setOnClickListener(
        v -> {
          PopupMenu menu = new PopupMenu(v.getContext(), v);
          menu.getMenu().add(0, 1, 0, com.example.mentor.ui.R.string.mentor_history_open);
          menu.getMenu().add(0, 2, 1, com.example.mentor.ui.R.string.mentor_history_delete);
          menu.setOnMenuItemClickListener(
              action -> {
                if (listener != null) {
                  if (action.getItemId() == 1) listener.onItemClick(position, item);
                  else listener.onDeleteClick(position, item);
                }
                return true;
              });
          menu.show();
        });
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static final class Holder extends RecyclerView.ViewHolder {
    final TextView title, preview, time, group;
    final View options, body;

    Holder(View root) {
      super(root);
      title = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_title);
      preview = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_preview);
      time = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_time);
      group = root.findViewById(com.example.mentor.ui.R.id.mentor_history_group);
      options = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_options);
      body = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_body);
      body.setFocusable(true);
    }
  }
}
