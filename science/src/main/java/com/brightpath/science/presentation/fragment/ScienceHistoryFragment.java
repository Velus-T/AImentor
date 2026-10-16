package com.brightpath.science.presentation.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.ScienceHistory;
import com.brightpath.science.presentation.ScienceHistoryActivity;
import com.brightpath.science.presentation.ScienceActivity;
import com.brightpath.science.presentation.adapter.ScienceModernAdapters;
import com.brightpath.science.util.ScienceUserSession;

import java.util.ArrayList;
import java.util.List;

public final class ScienceHistoryFragment extends Fragment {
    private final List<ScienceHistory> allItems = new ArrayList<>();
    private RecyclerView recycler;

        /*
 * Responsibility: Initializes ScienceHistoryFragment and receives or prepares the dependencies/state required by this Science UI tab/fragment.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceHistoryFragment() { super(R.layout.fragment_science_history); }

        /*
 * Responsibility: Binds the Fragment views after layout creation and configures adapters, listeners, and initial data.
 * Purpose: Connects the XML layout to Fragment behavior while keeping setup in the correct lifecycle stage.
 */
@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.buttonScienceHistoryBack).setOnClickListener(v -> requireActivity().finish());
        view.findViewById(R.id.buttonScienceHistoryAction).setOnClickListener(v -> startActivity(new Intent(requireContext(), ScienceHistoryActivity.class)));
        view.findViewById(R.id.buttonScienceHistoryFilter).setOnClickListener(v -> startActivity(new Intent(requireContext(), ScienceHistoryActivity.class)));

        recycler = view.findViewById(R.id.recyclerScienceHistory);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        int userId = new ScienceUserSession(requireContext()).getUserId();
        allItems.addAll(ScienceModule.get(requireContext()).historyRepository().search(userId, null, ""));
        showItems(allItems);

        bindChip(view, R.id.chipHistoryAll, 0);
        bindChip(view, R.id.chipHistoryToday, 1);
        bindChip(view, R.id.chipHistorySevenDays, 7);
        bindChip(view, R.id.chipHistoryMonth, 30);
    }

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindChip`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bindChip(View root, int id, int days) {
        TextView chip = root.findViewById(id);
        chip.setOnClickListener(v -> {
            resetChips(root);
            chip.setBackgroundResource(R.drawable.science_chip_green);
            chip.setTextColor(Color.WHITE);
            if (days == 0) {
                showItems(allItems);
                return;
            }
            long minimum = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L;
            List<ScienceHistory> filtered = new ArrayList<>();
            for (ScienceHistory item : allItems) if (item.getCreatedAt() >= minimum) filtered.add(item);
            showItems(filtered);
        });
    }

        /*
 * Responsibility: Handles the `resetChips` UI interaction/state operation for ScienceHistoryFragment.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void resetChips(View root) {
        int[] ids = {R.id.chipHistoryAll, R.id.chipHistoryToday, R.id.chipHistorySevenDays, R.id.chipHistoryMonth};
        for (int id : ids) {
            TextView chip = root.findViewById(id);
            chip.setBackgroundResource(R.drawable.science_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.science_text_secondary));
        }
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showItems` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showItems(List<ScienceHistory> items) {
        recycler.setAdapter(new ScienceModernAdapters.HistoryAdapter(items, false,
                item -> startActivity(ScienceActivity.createHistoryIntent(requireContext(), item))));
    }

}
