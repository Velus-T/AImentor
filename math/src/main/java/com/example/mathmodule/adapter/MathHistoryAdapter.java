package com.example.mathmodule.adapter;

import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mathmodule.databases.MathHistoryItem;
import com.example.mentor.ui.*;
import java.util.*;

/** Shared history rows with an expandable, locally rendered Math solution. */
public final class MathHistoryAdapter extends RecyclerView.Adapter<MathHistoryAdapter.Holder> {
  private List<MathHistoryItem> items;
  private final Set<Integer> expanded = new HashSet<>();

  public MathHistoryAdapter(List<MathHistoryItem> items) {
    this.items = new ArrayList<>(items);
  }

  public void updateData(List<MathHistoryItem> items) {
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
    MathHistoryItem item = items.get(position);
    String group = HistoryLabels.group(holder.itemView.getContext(), item.getTimestamp());
    holder.group.setText(group);
    holder.group.setVisibility(
        position == 0
                || !group.equals(
                    HistoryLabels.group(
                        holder.itemView.getContext(), items.get(position - 1).getTimestamp()))
            ? View.VISIBLE
            : View.GONE);
    holder.title.setText(item.getQuestion());
    holder.time.setText(TimeLabels.dateTime(holder.itemView.getContext(), item.getTimestamp()));
    holder.preview.setVisibility(View.GONE);
    holder.options.setVisibility(View.GONE);
    Runnable show =
        () -> {
          boolean visible = expanded.contains(item.getId());
          holder.detail.setVisibility(visible ? View.VISIBLE : View.GONE);
          holder.title.setMaxLines(visible ? Integer.MAX_VALUE : 2);
          if (visible) MathTextRenderer.render(holder.answer, item.getAnswer(), 15);
          holder.body.setContentDescription(
              item.getQuestion()
                  + ". "
                  + holder
                      .itemView
                      .getContext()
                      .getString(
                          visible
                              ? com.example.mentor.ui.R.string.mentor_history_hide_answer
                              : com.example.mentor.ui.R.string.mentor_history_show_answer));
        };
    holder.body.setOnClickListener(
        v -> {
          if (!expanded.add(item.getId())) expanded.remove(item.getId());
          show.run();
        });
    show.run();
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static final class Holder extends RecyclerView.ViewHolder {
    final TextView group, title, preview, time;
    final View options, body;
    final LinearLayout detail;
    final WebView answer;

    Holder(View root) {
      super(root);
      group = root.findViewById(com.example.mentor.ui.R.id.mentor_history_group);
      title = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_title);
      preview = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_preview);
      time = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_time);
      options = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_options);
      body = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_body);
      body.setFocusable(true);
      detail = root.findViewById(com.example.mentor.ui.R.id.mentor_history_row_detail);
      answer = new WebView(root.getContext());
      android.util.DisplayMetrics metrics = root.getResources().getDisplayMetrics();
      detail.addView(
          answer,
          new LinearLayout.LayoutParams(
              -1,
              Math.min(
                  Math.round(280 * metrics.density), Math.round(metrics.heightPixels * .45f))));
      answer.setOnTouchListener(
          (v, event) -> {
            int action = event.getActionMasked();
            v.getParent()
                .requestDisallowInterceptTouchEvent(
                    action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL);
            return false;
          });
    }
  }
}
