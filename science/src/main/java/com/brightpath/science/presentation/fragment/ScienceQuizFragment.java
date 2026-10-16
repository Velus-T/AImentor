package com.brightpath.science.presentation.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.QuizPerformance;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.QuizAttemptRepository;
import com.brightpath.science.presentation.ScienceMainActivity;
import com.brightpath.science.presentation.ScienceQuizActivity;
import com.brightpath.science.presentation.adapter.ScienceModernAdapters;
import com.brightpath.science.util.ScienceLevelCalculator;
import com.brightpath.science.util.SciencePreferenceStore;
import com.brightpath.science.util.ScienceUserSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ScienceQuizFragment extends Fragment {
        /*
 * Responsibility: Initializes ScienceQuizFragment and receives or prepares the dependencies/state required by this Science UI tab/fragment.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceQuizFragment() {
        super(R.layout.fragment_science_quiz);
    }

        /*
 * Responsibility: Binds the Fragment views after layout creation and configures adapters, listeners, and initial data.
 * Purpose: Connects the XML layout to Fragment behavior while keeping setup in the correct lifecycle stage.
 */
@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.buttonScienceQuizBack)
                .setOnClickListener(v -> requireActivity().finish());

        View.OnClickListener openPreferredSubject = v -> {
            SciencePreferenceStore store = new SciencePreferenceStore(requireContext());
            openQuiz(store.getSubject());
        };
        view.findViewById(R.id.buttonViewAllQuizTopics)
                .setOnClickListener(openPreferredSubject);
        view.findViewById(R.id.buttonViewAllRecentQuiz)
                .setOnClickListener(openPreferredSubject);

        refreshQuizSummary(view);
    }

        /*
 * Responsibility: Refreshes the current state/data when the component returns to the foreground.
 * Purpose: Ensures the UI reflects changes made on other Science screens.
 */
@Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            refreshQuizSummary(view);
        }
    }

        /*
 * Responsibility: Handles the `refreshQuizSummary` UI interaction/state operation for ScienceQuizFragment.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void refreshQuizSummary(@NonNull View view) {
        int userId = new ScienceUserSession(requireContext()).getUserId();
        QuizAttemptRepository repository =
                ScienceModule.get(requireContext()).quizAttemptRepository();

        QuizPerformance physics = QuizPerformance.empty();
        QuizPerformance chemistry = QuizPerformance.empty();
        QuizPerformance biology = QuizPerformance.empty();
        int totalExp = 0;

        try {
            physics = repository.getSubjectPerformance(userId, ScienceSubject.PHYSICS);
            chemistry = repository.getSubjectPerformance(userId, ScienceSubject.CHEMISTRY);
            biology = repository.getSubjectPerformance(userId, ScienceSubject.BIOLOGY);
            totalExp = repository.getTotalExp(userId);
        } catch (Exception ignored) {
            // Keep empty summaries so one database error cannot crash the Quiz screen.
        }

        bindLevel(view, totalExp);

        List<ScienceModernAdapters.QuizTopic> topics = Arrays.asList(
                toTopic(getString(ScienceSubject.PHYSICS.getDisplayNameResId()), ScienceSubject.PHYSICS, physics),
                toTopic(getString(ScienceSubject.CHEMISTRY.getDisplayNameResId()), ScienceSubject.CHEMISTRY, chemistry),
                toTopic(getString(ScienceSubject.BIOLOGY.getDisplayNameResId()), ScienceSubject.BIOLOGY, biology)
        );

        RecyclerView topicsView = view.findViewById(R.id.recyclerScienceQuizTopics);
        topicsView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        topicsView.setAdapter(
                new ScienceModernAdapters.QuizTopicAdapter(topics, this::openQuiz));

        int attempts = physics.getAttempts()
                + chemistry.getAttempts()
                + biology.getAttempts();
        int totalQuestions = physics.getTotalQuestions()
                + chemistry.getTotalQuestions()
                + biology.getTotalQuestions();
        int correctAnswers = physics.getCorrectAnswers()
                + chemistry.getCorrectAnswers()
                + biology.getCorrectAnswers();

        ScienceModernAdapters.RecentQuiz summary;
        if (totalQuestions == 0) {
            summary = new ScienceModernAdapters.RecentQuiz(
                    getString(R.string.science_quiz_summary_title),
                    getString(R.string.science_quiz_summary_empty),
                    "--",
                    getString(R.string.science_stat_no_data)
            );
        } else {
            int overallAccuracy = (int) Math.round(correctAnswers * 100.0 / totalQuestions);
            summary = new ScienceModernAdapters.RecentQuiz(
                    getString(R.string.science_quiz_overall_accuracy),
                    getString(R.string.science_quiz_attempts_format, attempts, totalQuestions),
                    getString(R.string.science_quiz_score_format_alt, correctAnswers, totalQuestions),
                    getString(R.string.science_quiz_accuracy_format, overallAccuracy)
            );
        }

        RecyclerView recentView = view.findViewById(R.id.recyclerScienceRecentQuiz);
        recentView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recentView.setAdapter(new ScienceModernAdapters.RecentQuizAdapter(
                Collections.singletonList(summary)));
    }

        /*
 * Responsibility: Converts input data in `toTopic` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
private ScienceModernAdapters.QuizTopic toTopic(
            @NonNull String title,
            @NonNull ScienceSubject subject,
            @NonNull QuizPerformance performance
    ) {
        return new ScienceModernAdapters.QuizTopic(
                title,
                subject,
                performance.getAttempts(),
                performance.getTotalQuestions(),
                performance.getCorrectAnswers()
        );
    }

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindLevel`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bindLevel(@NonNull View view, int totalExp) {
        int level = ScienceLevelCalculator.getLevel(totalExp);
        TextView expView = view.findViewById(R.id.tvScienceQuizTotalExp);
        TextView levelView = view.findViewById(R.id.tvScienceQuizLevelTitle);
        TextView levelProgressView = view.findViewById(R.id.tvScienceQuizLevelProgress);
        ProgressBar levelProgressBar = view.findViewById(R.id.progressScienceQuizLevel);

        expView.setText(getString(R.string.science_total_exp_format, totalExp));
        levelView.setText(getString(R.string.science_level_format, level) + " • "
                + getString(ScienceLevelCalculator.getTitleResId(level)));
        levelProgressBar.setProgress(
                ScienceLevelCalculator.getProgressPercent(totalExp));

        if (level >= ScienceLevelCalculator.MAX_LEVEL) {
            levelProgressView.setText(getString(R.string.science_level_max) + " • 6.555 EXP");
        } else {
            levelProgressView.setText(
                    getString(R.string.science_level_exp_format,
                            ScienceLevelCalculator.getExpInsideCurrentLevel(totalExp),
                            ScienceLevelCalculator.getExpToNextLevel(level))
                            + " • "
                            + getString(R.string.science_level_remaining_format,
                            ScienceLevelCalculator.getRemainingExp(totalExp)));
        }
    }

        /*
 * Responsibility: Creates the required navigation/action and opens the Science destination handled by `openQuiz`.
 * Purpose: Keeps navigation code separate from the business logic executed by the destination screen.
 */
private void openQuiz(@NonNull ScienceSubject subject) {
        SciencePreferenceStore store = new SciencePreferenceStore(requireContext());
        store.save(subject, store.getLevelPosition(), store.getStylePosition());
        startActivity(new Intent(requireContext(), ScienceQuizActivity.class));
    }

        /*
 * Responsibility: Provides the helper/reference operation named `main` for ScienceQuizFragment.
 * Purpose: Keeps this small responsibility isolated so the surrounding class remains easier to read and navigate during review.
 */
private ScienceMainActivity main() {
        return (ScienceMainActivity) requireActivity();
    }
}
