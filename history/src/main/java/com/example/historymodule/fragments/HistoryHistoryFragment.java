package com.example.historymodule.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.historymodule.HistoryChatActivity;
import com.example.historymodule.R;
import com.example.historymodule.adapter.HistoryHistoryAdapter;
import com.example.historymodule.databases.*;
import com.example.historymodule.util.HistoryUserSession;
import com.example.mentor.data.*;
import com.example.mentor.ui.HistoryControls;
import java.util.*;

/** Reopens real History conversations, with full-message search and explicit delete actions. */
public final class HistoryHistoryFragment extends Fragment {
  private final List<HistoryHistoryItem> all = new ArrayList<>(), visible = new ArrayList<>();
  private SqliteDbHelper database;
  private HistoryHistoryAdapter adapter;
  private HistoryControls controls;
  private ConversationSearchIndex search = new ConversationSearchIndex();
  private int userId;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
    return inflater.inflate(com.example.mentor.ui.R.layout.mentor_history_page, parent, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
    super.onViewCreated(root, state);
    userId = HistoryUserSession.getUserId(requireContext());
    database = new SqliteDbHelper(requireContext());
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_new)
        .setOnClickListener(
            v ->
                startActivity(
                    new Intent(requireContext(), HistoryChatActivity.class)
                        .putExtra(
                            HistoryChatActivity.EXTRA_CONVERSATION_ID, UUID.randomUUID().toString())
                        .putExtra(HistoryChatActivity.EXTRA_FOCUS_INPUT, true)));
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_heading).setVisibility(View.GONE);
    RecyclerView recycler = root.findViewById(com.example.mentor.ui.R.id.mentor_history_list);
    recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter =
        new HistoryHistoryAdapter(
            visible,
            new HistoryHistoryAdapter.Listener() {
              @Override
              public void onClick(HistoryHistoryItem item) {
                startActivity(
                    new Intent(requireContext(), HistoryChatActivity.class)
                        .putExtra(
                            HistoryChatActivity.EXTRA_CONVERSATION_ID, item.getReferenceId()));
              }

              @Override
              public void onLongClick(HistoryHistoryItem item) {
                confirmDelete(item);
              }
            });
    recycler.setAdapter(adapter);
    controls = new HistoryControls(root, state, this::filter);
    reload();
  }

  private void confirmDelete(HistoryHistoryItem item) {
    new AlertDialog.Builder(requireContext())
        .setTitle(R.string.history_delete_conv_title)
        .setMessage(R.string.history_delete_conv_msg)
        .setPositiveButton(
            android.R.string.ok,
            (d, w) -> {
              database.deleteConversation(userId, item.getReferenceId());
              reload();
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void reload() {
    all.clear();
    all.addAll(database.loadConversationSummaries(userId, 0, 0, 0));
    search = database.loadConversationSearchIndex(userId);
    filter();
  }

  private void filter() {
    if (controls == null || adapter == null) return;
    visible.clear();
    visible.addAll(
        controls.filter(
            all,
            item -> item.getTitle() + " " + search.text(item.getReferenceId()),
            item -> Timestamps.parse(item.getTimestamp())));
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (database != null) reload();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    if (controls != null) controls.save(out);
  }

  @Override
  public void onDestroyView() {
    if (database != null) database.close();
    database = null;
    adapter = null;
    controls = null;
    all.clear();
    visible.clear();
    search = new ConversationSearchIndex();
    super.onDestroyView();
  }
}
