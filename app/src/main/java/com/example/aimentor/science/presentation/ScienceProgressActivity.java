package com.example.aimentor.science.presentation;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.R;
import com.example.aimentor.science.di.ScienceModule;
import com.example.aimentor.science.domain.model.ScienceStats;
import com.example.aimentor.science.util.ScienceUserSession;

import java.util.Locale;

public final class ScienceProgressActivity extends com.example.aimentor.ui.InsetActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_progress);
        findViewById(R.id.btnProgressBack).setOnClickListener(view -> finish());
        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        if (findViewById(R.id.tvTotalQuestions) == null) return;
        int userId = new ScienceUserSession(this).getUserId();
        ScienceStats stats;
        try {
            stats = ScienceModule.get(this).getScienceStatsUseCase().execute(userId);
        } catch (Exception error) {
            ((TextView) findViewById(R.id.tvTotalQuestions)).setText("Progress is temporarily unavailable.");
            ((TextView) findViewById(R.id.tvMostStudied)).setText("Most studied: unavailable");
            ((TextView) findViewById(R.id.tvBookmarkCount)).setText("Bookmarks: unavailable");
            ((TextView) findViewById(R.id.tvQuizAccuracy)).setText("Quiz accuracy: unavailable");
            ((TextView) findViewById(R.id.tvReviewTime)).setText("Review time: unavailable");
            ((TextView) findViewById(R.id.tvProgressInsight)).setText(
                    "The local Science database could not be read. Your other app modules are unaffected.");
            return;
        }
        ((TextView) findViewById(R.id.tvTotalQuestions))
                .setText("Questions asked: " + stats.getTotalQuestions());
        ((TextView) findViewById(R.id.tvMostStudied))
                .setText(stats.getTotalQuestions() == 0
                        ? "Most studied: Not enough data"
                        : "Most studied: " + stats.getMostStudiedSubject().getDisplayName());
        ((TextView) findViewById(R.id.tvBookmarkCount))
                .setText("Bookmarks: " + stats.getBookmarkCount());
        ((TextView) findViewById(R.id.tvQuizAccuracy))
                .setText(String.format(Locale.getDefault(),
                        "Quiz accuracy: %.0f%% across %d attempt(s)",
                        stats.getQuizAccuracy(), stats.getQuizAttempts()));
        ((TextView) findViewById(R.id.tvReviewTime))
                .setText("Recorded answer review time: " + formatDuration(stats.getReviewSeconds()));

        String insight;
        if (stats.getTotalQuestions() == 0) {
            insight = "Ask your first Physics, Chemistry or Biology question to begin tracking progress.";
        } else if (stats.getQuizAttempts() == 0) {
            insight = "You study " + stats.getMostStudiedSubject().getDisplayName()
                    + " most often. Try a short quiz to check understanding.";
        } else if (stats.getQuizAccuracy() < 60) {
            insight = "Review bookmarked explanations and common mistakes, then retry a quiz on the same topic.";
        } else {
            insight = "Good progress. Continue with a harder follow-up question or a mixed-format quiz.";
        }
        ((TextView) findViewById(R.id.tvProgressInsight)).setText(insight);
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return minutes + "m " + remainingSeconds + "s";
    }
}
