package com.example.mathmodule.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.mathmodule.MathChatActivity;
import com.example.mathmodule.R;
import com.example.mathmodule.adapter.MathHistoryAdapter;
import com.example.mathmodule.databases.*;
import com.example.mentor.data.Timestamps;
import com.example.mentor.ui.HistoryControls;
import java.util.*;

/** A searchable saved-question list; answers retain their full formula rendering. */
public final class MathHistoryFragment extends Fragment {
  private final List<MathHistoryItem> all = new ArrayList<>();
  private SqliteDbHelper database;
  private MathHistoryAdapter adapter;
  private HistoryControls controls;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
    return inflater.inflate(com.example.mentor.ui.R.layout.mentor_history_page, parent, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
    super.onViewCreated(root, state);
    database = new SqliteDbHelper(requireContext());
    ((android.widget.TextView) root.findViewById(com.example.mentor.ui.R.id.mentor_history_heading))
        .setText(R.string.math_history_section_title);
    android.widget.Button open = root.findViewById(com.example.mentor.ui.R.id.mentor_history_new);
    open.setText(com.example.mentor.ui.R.string.mentor_history_open_assistant);
    open.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), MathChatActivity.class)));
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_back).setVisibility(View.VISIBLE);
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_back)
        .setOnClickListener(v -> requireActivity().finish());
    RecyclerView recycler = root.findViewById(com.example.mentor.ui.R.id.mentor_history_list);
    recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter = new MathHistoryAdapter(new ArrayList<>());
    recycler.setAdapter(adapter);
    controls = new HistoryControls(root, state, this::filter);
    reload();
  }

  private void reload() {
    all.clear();
    all.addAll(database.getAllHistory());
    filter();
  }

  private void filter() {
    if (controls != null && adapter != null)
      adapter.updateData(
          controls.filter(
              all,
              item -> item.getQuestion() + " " + item.getAnswer(),
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
    super.onDestroyView();
  }
}
