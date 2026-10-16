package com.brightpath.science.presentation.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.presentation.CircuitDiagramActivity;
import com.brightpath.science.presentation.PeriodicTableActivity;
import com.brightpath.science.presentation.ScienceActivity;
import com.brightpath.science.presentation.ScienceMainActivity;
import com.brightpath.science.presentation.ScienceProgressActivity;
import com.brightpath.science.util.ScienceLevelCalculator;
import com.brightpath.science.util.SciencePreferenceStore;

public final class ScienceHomeFragment extends Fragment {
        /*
 * Responsibility: Initializes ScienceHomeFragment and receives or prepares the dependencies/state required by this Science UI tab/fragment.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceHomeFragment() { super(R.layout.fragment_science_home); }

        /*
 * Responsibility: Binds the Fragment views after layout creation and configures adapters, listeners, and initial data.
 * Purpose: Connects the XML layout to Fragment behavior while keeping setup in the correct lifecycle stage.
 */
@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.buttonHomeBack).setOnClickListener(v -> requireActivity().finish());
        view.findViewById(R.id.buttonHomeAction).setOnClickListener(v -> main().selectTab(R.id.navScienceHistory));
        view.findViewById(R.id.cardHomePhysics).setOnClickListener(v -> openChat(ScienceSubject.PHYSICS));
        view.findViewById(R.id.cardHomeChemistry).setOnClickListener(v -> openChat(ScienceSubject.CHEMISTRY));
        view.findViewById(R.id.cardHomeBiology).setOnClickListener(v -> openChat(ScienceSubject.BIOLOGY));
        view.findViewById(R.id.cardHomePeriodicTable).setOnClickListener(v -> startActivity(new Intent(requireContext(), PeriodicTableActivity.class)));
        view.findViewById(R.id.cardHomeCircuit).setOnClickListener(v -> startActivity(new Intent(requireContext(), CircuitDiagramActivity.class)));
        view.findViewById(R.id.cardHomeProgress).setOnClickListener(v -> startActivity(new Intent(requireContext(), ScienceProgressActivity.class)));
        updateExp(view);
    }

        /*
 * Responsibility: Refreshes the current state/data when the component returns to the foreground.
 * Purpose: Ensures the UI reflects changes made on other Science screens.
 */
@Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) updateExp(view);
    }

        /*
 * Responsibility: Updates the state or value handled by `updateExp` for ScienceHomeFragment.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void updateExp(@NonNull View view) {
        int userId = new com.brightpath.science.util.ScienceUserSession(requireContext()).getUserId();
        int totalExp;
        try {
            totalExp = ScienceModule.get(requireContext())
                    .quizAttemptRepository().getTotalExp(userId);
        } catch (Exception ignored) {
            totalExp = 0;
        }
        int level = ScienceLevelCalculator.getLevel(totalExp);
        String levelTitle = getString(ScienceLevelCalculator.getTitleResId(level));
        ((TextView) view.findViewById(R.id.tvHomeScienceExp))
                .setText(getString(R.string.science_level_format, level) + " • "
                        + levelTitle
                        + "\n" + getString(R.string.science_total_exp_format, totalExp));
    }

        /*
 * Responsibility: Creates the required navigation/action and opens the Science destination handled by `openChat`.
 * Purpose: Keeps navigation code separate from the business logic executed by the destination screen.
 */
private void openChat(ScienceSubject subject) {
        SciencePreferenceStore store = new SciencePreferenceStore(requireContext());
        store.save(subject, store.getLevelPosition(), store.getStylePosition());
        Intent intent = new Intent(requireContext(), ScienceActivity.class);
        intent.putExtra(ScienceActivity.EXTRA_SUBJECT, subject.getCode());
        startActivity(intent);
    }

        /*
 * Responsibility: Provides the helper/reference operation named `main` for ScienceHomeFragment.
 * Purpose: Keeps this small responsibility isolated so the surrounding class remains easier to read and navigate during review.
 */
private ScienceMainActivity main() { return (ScienceMainActivity) requireActivity(); }
}
