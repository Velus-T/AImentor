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
                .setText(formatSubjectQuiz("Vật lý", physics));
        ((TextView) findViewById(R.id.tvQuizAccuracyChemistry))
                .setText(formatSubjectQuiz("Hóa học", chemistry));
        ((TextView) findViewById(R.id.tvQuizAccuracyBiology))
                .setText(formatSubjectQuiz("Sinh học", biology));

        ((TextView) findViewById(R.id.tvScienceExp))
                .setText("Tổng EXP Khoa học: " + totalExp);
        ((TextView) findViewById(R.id.tvReviewTime))
                .setText("Thời gian xem lại câu trả lời: "
                        + formatDuration(stats.getReviewSeconds()));

        String insight;
        if (stats.getQuizAttempts() == 0) {
            insight = "Hoàn thành Quiz để nhận EXP và bắt đầu tăng cấp Khoa học.";
        } else if (stats.getQuizAccuracy() < 60) {
            insight = "Hãy xem lại các câu trả lời sai trước khi thử Quiz khó hơn. "
                    + "Câu khó thưởng nhiều EXP nhưng chỉ có lợi khi trả lời đúng.";
        } else if (level < 5) {
            insight = "Tiến độ đang ổn. Hãy duy trì Quiz đều đặn để đạt danh hiệu "
                    + "Học viên Khoa học ở cấp 5.";
        } else if (level < 13) {
            insight = "Bạn đã xây dựng nền tảng tốt. Hãy kết hợp Quiz Vật lý, Hóa học "
                    + "và Sinh học để tăng cấp cân bằng.";
        } else {
            insight = "Cấp độ hiện tại rất tốt. Ưu tiên câu khó và Quiz tổng hợp để "
                    + "tiến tới Bậc thầy Khoa học.";
        }
        ((TextView) findViewById(R.id.tvProgressInsight)).setText(insight);
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `formatSubjectQuiz` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String formatSubjectQuiz(String subjectName, QuizPerformance performance) {
        if (performance == null || performance.getTotalQuestions() == 0) {
            return subjectName + ": Chưa có dữ liệu Quiz";
        }

        return String.format(Locale.getDefault(),
                "%s: %.0f%% • %d/%d câu đúng • %d lượt",
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
                .setText("Cấp độ tạm thời không khả dụng");
        ((ProgressBar) findViewById(R.id.progressScienceLevel)).setProgress(0);
        ((TextView) findViewById(R.id.tvScienceLevelProgress)).setText("0 / 120 EXP");
        ((TextView) findViewById(R.id.tvScienceLevelTotalExp))
                .setText("Tổng EXP Khoa học: không khả dụng");
        ((TextView) findViewById(R.id.tvTotalQuestions))
                .setText("Tiến độ tạm thời không khả dụng.");
        ((TextView) findViewById(R.id.tvMostStudied))
                .setText("Môn học nhiều nhất: không khả dụng");
        ((TextView) findViewById(R.id.tvBookmarkCount))
                .setText("Câu trả lời đã lưu: không khả dụng");
        ((TextView) findViewById(R.id.tvQuizAccuracy))
                .setText("Độ chính xác Quiz toàn module: không khả dụng");
        ((TextView) findViewById(R.id.tvQuizAccuracyPhysics))
                .setText("Vật lý: không khả dụng");
        ((TextView) findViewById(R.id.tvQuizAccuracyChemistry))
                .setText("Hóa học: không khả dụng");
        ((TextView) findViewById(R.id.tvQuizAccuracyBiology))
                .setText("Sinh học: không khả dụng");
        ((TextView) findViewById(R.id.tvScienceExp))
                .setText("Tổng EXP Khoa học: không khả dụng");
        ((TextView) findViewById(R.id.tvReviewTime))
                .setText("Thời gian xem lại: không khả dụng");
        ((TextView) findViewById(R.id.tvProgressInsight))
                .setText("Không thể đọc cơ sở dữ liệu Khoa học trên thiết bị này.");
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `formatDuration` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return minutes + " phút " + remainingSeconds + " giây";
    }
}
