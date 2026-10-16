package com.brightpath.science.presentation;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.QuizPerformance;
import com.brightpath.science.domain.model.ScienceStats;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.QuizAttemptRepository;
import com.brightpath.science.util.ScienceLevelCalculator;
import com.brightpath.science.util.ScienceUserSession;

import java.util.Locale;

public final class ScienceProgressActivity extends AppCompatActivity {
        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_progress);
        findViewById(R.id.btnProgressBack).setOnClickListener(view -> finish());
        loadStats();
    }

        /*
 * Responsibility: Refreshes the current state/data when the component returns to the foreground.
 * Purpose: Ensures the UI reflects changes made on other Science screens.
 */
@Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

        /*
 * Responsibility: Retrieves the data/state required by `loadStats` from the appropriate Android screen and user-interaction controller.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
private void loadStats() {
        if (findViewById(R.id.tvTotalQuestions) == null) {
            return;
        }

        int userId = new ScienceUserSession(this).getUserId();
        ScienceStats stats;
        QuizPerformance physics;
        QuizPerformance chemistry;
        QuizPerformance biology;

        try {
            ScienceModule module = ScienceModule.get(this);
            stats = module.getScienceStatsUseCase().execute(userId);

            QuizAttemptRepository quizRepository = module.quizAttemptRepository();
            physics = quizRepository.getSubjectPerformance(userId, ScienceSubject.PHYSICS);
            chemistry = quizRepository.getSubjectPerformance(userId, ScienceSubject.CHEMISTRY);
            biology = quizRepository.getSubjectPerformance(userId, ScienceSubject.BIOLOGY);
        } catch (Exception error) {
            showUnavailableState();
            return;
        }

        int totalExp = stats.getTotalScienceExp();
        int level = ScienceLevelCalculator.getLevel(totalExp);

        ((TextView) findViewById(R.id.tvScienceLevelTitle))
                .setText(getString(R.string.science_level_format, level) + " • "
                        + getString(ScienceLevelCalculator.getTitleResId(level)));
        ((ProgressBar) findViewById(R.id.progressScienceLevel))
                .setProgress(ScienceLevelCalculator.getProgressPercent(totalExp));

        TextView levelProgressView = findViewById(R.id.tvScienceLevelProgress);
        if (level >= ScienceLevelCalculator.MAX_LEVEL) {
            levelProgressView.setText(getString(R.string.science_level_max) + " • 6.555 EXP");
        } else {
            levelProgressView.setText(getString(R.string.science_level_exp_format,
                            ScienceLevelCalculator.getExpInsideCurrentLevel(totalExp),
                            ScienceLevelCalculator.getExpToNextLevel(level))
                            + " • "
                            + getString(R.string.science_level_remaining_format,
                            ScienceLevelCalculator.getRemainingExp(totalExp)));
        }

        ((TextView) findViewById(R.id.tvScienceLevelTotalExp))
                .setText(getString(R.string.science_total_exp_format, totalExp));
        ((TextView) findViewById(R.id.tvTotalQuestions))
                .setText(getString(R.string.science_stat_questions, stats.getTotalQuestions()));

        String mostStudied = stats.getTotalQuestions() == 0
                ? getString(R.string.science_stat_no_data)
                : getString(stats.getMostStudiedSubject().getDisplayNameResId());
        ((TextView) findViewById(R.id.tvMostStudied))
                .setText(getString(R.string.science_stat_most_studied, mostStudied));

        ((TextView) findViewById(R.id.tvBookmarkCount))
                .setText(getString(R.string.science_stat_bookmarks, stats.getBookmarkCount()));

        TextView overallAccuracyView = findViewById(R.id.tvQuizAccuracy);
        if (stats.getQuizAttempts() == 0) {
            overallAccuracyView.setText(getString(R.string.science_stat_accuracy, 0f, 0).split(":")[0] + ": " + getString(R.string.science_stat_no_data));
        } else {
            overallAccuracyView.setText(getString(R.string.science_stat_accuracy,
                    stats.getQuizAccuracy(), stats.getQuizAttempts()));
        }

        ((TextView) findViewById(R.id.tvQuizAccuracyPhysics))
                .setText(formatSubjectQuiz(getString(R.string.science_progress_physics), physics));
        ((TextView) findViewById(R.id.tvQuizAccuracyChemistry))
                .setText(formatSubjectQuiz(getString(R.string.science_progress_chemistry), chemistry));
        ((TextView) findViewById(R.id.tvQuizAccuracyBiology))
                .setText(formatSubjectQuiz(getString(R.string.science_progress_biology), biology));

        ((TextView) findViewById(R.id.tvScienceExp))
                .setText(getString(R.string.science_total_exp_format, totalExp));
        ((TextView) findViewById(R.id.tvReviewTime))
                .setText(getString(R.string.science_review_duration, formatDuration(stats.getReviewSeconds())));

        String insight;
        if (stats.getQuizAttempts() == 0) {
            insight = getString(R.string.science_progress_complete_a_quiz_to_earn_exp_and);
        } else if (stats.getQuizAccuracy() < 60) {
            insight = getString(R.string.science_progress_review_incorrect_answers_before_trying_harder_quizzes);
        } else if (level < 5) {
            insight = getString(R.string.science_progress_keep_practicing_quizzes_to_reach_science_learner);
        } else if (level < 13) {
            insight = getString(R.string.science_progress_combine_physics_chemistry_and_biology_quizzes_for);
        } else {
            insight = getString(R.string.science_progress_try_harder_and_mixed_quizzes_to_work);
        }
        ((TextView) findViewById(R.id.tvProgressInsight)).setText(insight);
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `formatSubjectQuiz` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String formatSubjectQuiz(String subjectName, QuizPerformance performance) {
        if (performance == null || performance.getTotalQuestions() == 0) {
            return getString(R.string.science_subject_no_quiz, subjectName);
        }

        return getString(R.string.science_subject_quiz_summary,
                subjectName,
                performance.getAccuracyPercent(),
                performance.getCorrectAnswers(),
                performance.getTotalQuestions(),
                performance.getAttempts());
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showUnavailableState` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showUnavailableState() {
        ((TextView) findViewById(R.id.tvScienceLevelTitle))
                .setText(getString(R.string.science_progress_level_temporarily_unavailable));
        ((ProgressBar) findViewById(R.id.progressScienceLevel)).setProgress(0);
        ((TextView) findViewById(R.id.tvScienceLevelProgress)).setText("0 / 120 EXP");
        ((TextView) findViewById(R.id.tvScienceLevelTotalExp))
                .setText(getString(R.string.science_progress_science_exp_unavailable));
        ((TextView) findViewById(R.id.tvTotalQuestions))
                .setText(getString(R.string.science_progress_progress_temporarily_unavailable));
        ((TextView) findViewById(R.id.tvMostStudied))
                .setText(getString(R.string.science_progress_most_studied_subject_unavailable));
        ((TextView) findViewById(R.id.tvBookmarkCount))
                .setText(getString(R.string.science_progress_saved_answers_unavailable));
        ((TextView) findViewById(R.id.tvQuizAccuracy))
                .setText(getString(R.string.science_progress_overall_quiz_accuracy_unavailable));
        ((TextView) findViewById(R.id.tvQuizAccuracyPhysics))
                .setText(getString(R.string.science_progress_physics_unavailable));
        ((TextView) findViewById(R.id.tvQuizAccuracyChemistry))
                .setText(getString(R.string.science_progress_chemistry_unavailable));
        ((TextView) findViewById(R.id.tvQuizAccuracyBiology))
                .setText(getString(R.string.science_progress_biology_unavailable));
        ((TextView) findViewById(R.id.tvScienceExp))
                .setText(getString(R.string.science_progress_science_exp_unavailable));
        ((TextView) findViewById(R.id.tvReviewTime))
                .setText(getString(R.string.science_progress_review_time_unavailable));
        ((TextView) findViewById(R.id.tvProgressInsight))
                .setText(getString(R.string.science_progress_unable_to_read_the_science_database_on));
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `formatDuration` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return getString(R.string.science_duration_parts, minutes, remainingSeconds);
    }
}
