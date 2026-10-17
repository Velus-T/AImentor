package com.brightpath.science.presentation.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.presentation.ScienceActivity;
import com.brightpath.science.presentation.adapter.ScienceModernAdapters;
import com.brightpath.science.util.ScienceUserSession;
import com.example.mentor.ui.HistoryControls;
import java.util.*;

/** Actual search/filter controls for saved Science questions; refreshes on every return. */
public final class ScienceHistoryFragment extends Fragment {
  private final List<ScienceHistory> all = new ArrayList<>();
  private RecyclerView recycler;
  private HistoryControls controls;

  public ScienceHistoryFragment() {
    super(com.example.mentor.ui.R.layout.mentor_history_page);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
    super.onViewCreated(root, state);
    android.widget.Button open = root.findViewById(com.example.mentor.ui.R.id.mentor_history_new);
    open.setText(com.example.mentor.ui.R.string.mentor_history_open_assistant);
    open.setOnClickListener(
        v -> startActivity(new Intent(requireContext(), ScienceActivity.class)));
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_back).setVisibility(View.VISIBLE);
    root.findViewById(com.example.mentor.ui.R.id.mentor_history_back)
        .setOnClickListener(v -> requireActivity().finish());
    recycler = root.findViewById(com.example.mentor.ui.R.id.mentor_history_list);
    recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
    controls = new HistoryControls(root, state, this::filter);
    reload();
  }

  private void reload() {
    int userId = new ScienceUserSession(requireContext()).getUserId();
    all.clear();
    all.addAll(ScienceModule.get(requireContext()).historyRepository().search(userId, null, ""));
    filter();
  }

  private void filter() {
    if (controls == null || recycler == null) return;
    List<ScienceHistory> visible =
        controls.filter(
            all, item -> item.getQuestion() + " " + item.getAnswer(), ScienceHistory::getCreatedAt);
    recycler.setAdapter(
        new ScienceModernAdapters.HistoryAdapter(
            visible,
            false,
            item -> startActivity(ScienceActivity.createHistoryIntent(requireContext(), item))));
  }

  @Override
  public void onResume() {
    super.onResume();
    if (controls != null) reload();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    if (controls != null) controls.save(out);
  }

  @Override
  public void onDestroyView() {
    controls = null;
    recycler = null;
    all.clear();
    super.onDestroyView();
  }
}
