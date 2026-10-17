package com.example.modulelaptrinh.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.mentor.data.*;
import com.example.mentor.ui.HistoryControls;
import com.example.modulelaptrinh.MainActivity;
import com.example.modulelaptrinh.ProgrammingChatActivity;
import com.example.modulelaptrinh.R;
import com.example.modulelaptrinh.adapters.ProgrammingChatHistoryAdapter;
import com.example.modulelaptrinh.database.*;
import com.example.modulelaptrinh.util.ConfirmDeletion;
import java.util.*;

/** Full-message search and shared conversation rows; existing single/all deletion is retained. */
public final class ChatHistoryFragment extends Fragment {
  private final List<SessionItem> all = new ArrayList<>();
  private ProgrammingDbHelper database;
  private ProgrammingChatHistoryAdapter adapter;
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
    userId = ((MainActivity) requireActivity()).getCurrentUserId();
    database = new ProgrammingDbHelper(requireContext());
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_new)
        .setOnClickListener(v -> open(null));
    View delete = root.findViewById(com.example.mentor.ui.R.id.mentor_history_delete_all);
    delete.setVisibility(View.VISIBLE);
    delete.setOnClickListener(
        v -> {
          android.widget.PopupMenu menu = new android.widget.PopupMenu(requireContext(), v);
          menu.getMenu()
              .add(com.example.mentor.ui.R.string.mentor_history_delete_all)
              .setEnabled(!all.isEmpty());
          menu.setOnMenuItemClickListener(
              item -> {
                ConfirmDeletion.show(
                    requireContext(),
                    R.string.program_delete_all_chats,
                    () -> database.deleteAllSessions(userId),
                    this::reload);
                return true;
              });
          menu.show();
        });
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_heading).setVisibility(View.GONE);
    RecyclerView recycler = root.findViewById(com.example.mentor.ui.R.id.mentor_history_list);
    recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter =
        new ProgrammingChatHistoryAdapter(
            new ArrayList<>(),
            new ProgrammingChatHistoryAdapter.OnDeleteClickListener() {
              @Override
              public void onItemClick(int position, SessionItem item) {
                open(item);
              }

              @Override
              public void onDeleteClick(int position, SessionItem item) {
                ConfirmDeletion.show(
                    requireContext(),
                    R.string.program_delete_chat,
                    () -> database.deleteSession(item.getId(), userId),
                    ChatHistoryFragment.this::reload);
              }
            });
    recycler.setAdapter(adapter);
    controls = new HistoryControls(root, state, this::filter);
    reload();
  }

  private void open(SessionItem item) {
    Intent intent =
        new Intent(requireContext(), ProgrammingChatActivity.class)
            .putExtra(MainActivity.EXTRA_USER_ID, userId);
    if (item != null) intent.putExtra("SESSION_ID", item.getId());
    startActivity(intent);
  }

  private void reload() {
    all.clear();
    all.addAll(database.getAllSessions(userId));
    search = database.loadSessionSearchIndex(userId);
    for (SessionItem item : all) {
      String name = item.getName();
      if (name == null
          || name.trim().isEmpty()
          || "Cuộc trò chuyện mới".equals(name)
          || "New chat".equals(name)) {
        String first = search.firstUser(String.valueOf(item.getId())).replace('\n', ' ');
        if (!first.isEmpty())
          item.setName(
              first.codePointCount(0, first.length()) > 64
                  ? first.substring(0, first.offsetByCodePoints(0, 64)) + "…"
                  : first);
      }
    }
    filter();
  }

  private void filter() {
    if (controls != null && adapter != null)
      adapter.updateData(
          controls.filter(
              all,
              item -> item.getName() + " " + search.text(String.valueOf(item.getId())),
              item -> Timestamps.parse(item.getTimestamp())));
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
    search = new ConversationSearchIndex();
    super.onDestroyView();
  }
}
