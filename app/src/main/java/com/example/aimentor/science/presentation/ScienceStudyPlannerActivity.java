package com.example.aimentor.science.presentation;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.R;
import com.example.aimentor.science.di.ScienceModule;
import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.model.StudyPlan;
import com.example.aimentor.science.domain.model.StudyPlanRequest;
import com.example.aimentor.science.domain.model.StudySession;
import com.example.aimentor.science.domain.repository.StudyPlanRepository;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.usecase.GenerateStudyPlanUseCase;
import com.example.aimentor.science.util.SciencePreferenceStore;
import com.example.aimentor.science.util.ScienceUserSession;
import com.example.aimentor.science.util.ScienceUiFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Generates and tracks a Science schedule beginning at the generation moment. */
public final class ScienceStudyPlannerActivity extends com.example.aimentor.ui.InsetActivity {
    private static final int[] DAY_VALUES = {3, 7, 14, 21, 30};
    private static final int[] MINUTE_VALUES = {30, 45, 60, 90, 120};

    private Spinner subjectSpinner;
    private Spinner levelSpinner;
    private Spinner daysSpinner;
    private Spinner minutesSpinner;
    private EditText goalInput;
    private Button generateButton;
    private Button clearButton;
    private ProgressBar progressBar;
    private TextView statusView;
    private LinearLayout sessionsLayout;

    private GenerateStudyPlanUseCase generateUseCase;
    private StudyPlanRepository repository;
    private int userId;
    private StudyPlan activePlan;
    private boolean loading;
    private volatile boolean destroyed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_study_planner);

        ScienceModule module = ScienceModule.get(this);
        generateUseCase = module.generateStudyPlanUseCase();
        repository = module.studyPlanRepository();
        userId = new ScienceUserSession(this).getUserId();

        bindViews();
        setupSpinners();
        setupActions();
        try {
            showPlan(repository.getLatest(userId));
        } catch (Exception error) {
            showPlan(null);
            statusView.setTextColor(getColor(R.color.science_error));
            statusView.setText("The saved study plan could not be opened safely.");
            Toast.makeText(this, "Unable to read the saved study plan.", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindViews() {
        subjectSpinner = findViewById(R.id.spPlannerSubject);
        levelSpinner = findViewById(R.id.spPlannerLevel);
        daysSpinner = findViewById(R.id.spPlannerDays);
        minutesSpinner = findViewById(R.id.spPlannerMinutes);
        goalInput = findViewById(R.id.etPlannerGoal);
        generateButton = findViewById(R.id.btnGenerateStudyPlan);
        clearButton = findViewById(R.id.btnClearStudyPlan);
        progressBar = findViewById(R.id.progressStudyPlan);
        statusView = findViewById(R.id.tvStudyPlanStatus);
        sessionsLayout = findViewById(R.id.layoutStudySessions);
    }

    private void setupSpinners() {
        subjectSpinner.setAdapter(adapter(new String[]{"Physics", "Chemistry", "Biology"}));
        String[] levels = new String[EducationLevel.values().length];
        for (int i = 0; i < levels.length; i++) levels[i] = EducationLevel.values()[i].getDisplayName();
        levelSpinner.setAdapter(adapter(levels));
        daysSpinner.setAdapter(adapter(new String[]{"3 days", "7 days", "14 days", "21 days", "30 days"}));
        minutesSpinner.setAdapter(adapter(new String[]{"30 min/day", "45 min/day", "60 min/day", "90 min/day", "120 min/day"}));

        SciencePreferenceStore preferences = new SciencePreferenceStore(this);
        subjectSpinner.setSelection(preferences.getSubject().ordinal());
        levelSpinner.setSelection(preferences.getLevelPosition());
        daysSpinner.setSelection(1);
        minutesSpinner.setSelection(2);
    }

    private android.widget.ArrayAdapter<String> adapter(String[] values) {
        return ScienceUiFactory.spinnerAdapter(this, values);
    }

    private void setupActions() {
        findViewById(R.id.btnStudyPlannerBack).setOnClickListener(view -> finish());
        generateButton.setOnClickListener(view -> generatePlan());
        clearButton.setOnClickListener(view -> confirmClear());
    }

    private void generatePlan() {
        if (loading) return;
        setLoading(true);
        long startMoment = System.currentTimeMillis();
        StudyPlanRequest request = new StudyPlanRequest(
                userId,
                selectedSubject(),
                EducationLevel.fromPosition(levelSpinner.getSelectedItemPosition()),
                goalInput.getText().toString(),
                DAY_VALUES[Math.max(0, Math.min(daysSpinner.getSelectedItemPosition(), DAY_VALUES.length - 1))],
                MINUTE_VALUES[Math.max(0, Math.min(minutesSpinner.getSelectedItemPosition(), MINUTE_VALUES.length - 1))],
                startMoment
        );
        statusView.setText("Creating a schedule from " + formatDateTime(startMoment) + "…");
        generateUseCase.execute(request, new ResultCallback<StudyPlan>() {
            @Override
            public void onSuccess(StudyPlan result) {
                if (destroyed) return;
                setLoading(false);
                showPlan(result);
                Toast.makeText(ScienceStudyPlannerActivity.this,
                        "Study plan created from the current time.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message, Throwable error) {
                if (destroyed) return;
                setLoading(false);
                statusView.setText(message == null ? "Unable to create a study plan." : message);
                statusView.setTextColor(getColor(R.color.science_error));
            }
        });
    }

    private void showPlan(@Nullable StudyPlan plan) {
        activePlan = plan;
        sessionsLayout.removeAllViews();
        clearButton.setVisibility(plan == null ? View.GONE : View.VISIBLE);
        if (plan == null || plan.getSessions().isEmpty()) {
            statusView.setTextColor(getColor(R.color.science_text_secondary));
            statusView.setText("No study plan has been generated yet.");
            return;
        }

        String source = plan.isGeneratedByAi()
                ? "AI-personalized plan" : "Reliable local fallback plan";
        statusView.setTextColor(getColor(R.color.science_text_primary));
        statusView.setText(source + " · created " + formatDateTime(plan.getCreatedAtMillis())
                + "\nFirst session: " + formatDateTime(plan.getSessions().get(0).getStartAtMillis())
                + "\n" + plan.getSubject().getDisplayName() + " · "
                + plan.getLevel().getDisplayName() + " · Goal: " + plan.getGoal());

        for (StudySession session : plan.getSessions()) addSessionView(session);
    }

    private void addSessionView(StudySession session) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundResource(R.drawable.science_panel);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);

        CheckBox completed = new CheckBox(this);
        completed.setMinHeight(dp(48));
        completed.setButtonTintList(getColorStateList(R.color.science_control_tint));
        completed.setTextColor(getColor(R.color.science_text_primary));
        completed.setText("Day " + session.getDayNumber() + " · " + session.getTitle());
        completed.setTextSize(16f);
        completed.setTypeface(completed.getTypeface(), Typeface.BOLD);
        completed.setChecked(session.isCompleted());
        card.addView(completed);

        TextView time = new TextView(this);
        time.setText(formatDateTime(session.getStartAtMillis())
                + " · " + session.getDurationMinutes() + " minutes");
        time.setTextColor(getColor(R.color.science_primary_dark));
        time.setTextSize(14f);
        time.setPadding(dp(48), 0, 0, dp(6));
        card.addView(time);

        TextView activities = new TextView(this);
        activities.setText(session.getActivities());
        activities.setTextColor(getColor(R.color.science_text_secondary));
        activities.setTextSize(14f);
        activities.setLineSpacing(0f, 1.15f);
        activities.setPadding(dp(48), 0, 0, dp(4));
        card.addView(activities);

        addSafeCompletionListener(completed, session);
        sessionsLayout.addView(card);
    }

    private void addSafeCompletionListener(CompoundButton checkBox, StudySession session) {
        checkBox.setOnCheckedChangeListener((button, checked) -> {
            boolean previous = session.isCompleted();
            session.setCompleted(checked);
            if (activePlan == null) return;
            try {
                repository.save(userId, activePlan);
            } catch (Exception error) {
                session.setCompleted(previous);
                button.setOnCheckedChangeListener(null);
                button.setChecked(previous);
                addSafeCompletionListener(button, session);
                Toast.makeText(this, "Unable to save this change.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ScienceSubject selectedSubject() {
        int position = subjectSpinner.getSelectedItemPosition();
        if (position == 1) return ScienceSubject.CHEMISTRY;
        if (position == 2) return ScienceSubject.BIOLOGY;
        return ScienceSubject.PHYSICS;
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle("Clear saved study plan?")
                .setMessage("Completed session checks will also be removed.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    try {
                        repository.clear(userId);
                        showPlan(null);
                    } catch (Exception error) {
                        Toast.makeText(this, "Unable to clear the saved plan.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void setLoading(boolean value) {
        if (destroyed) return;
        loading = value;
        progressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        generateButton.setEnabled(!value);
        subjectSpinner.setEnabled(!value);
        levelSpinner.setEnabled(!value);
        daysSpinner.setEnabled(!value);
        minutesSpinner.setEnabled(!value);
        goalInput.setEnabled(!value);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("EEE, dd MMM yyyy · HH:mm", Locale.getDefault())
                .format(new Date(millis));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
