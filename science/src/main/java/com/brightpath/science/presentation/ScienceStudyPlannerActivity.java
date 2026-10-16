package com.brightpath.science.presentation;

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

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.model.StudyPlan;
import com.brightpath.science.domain.model.StudyPlanRequest;
import com.brightpath.science.domain.model.StudySession;
import com.brightpath.science.domain.repository.StudyPlanRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.usecase.GenerateStudyPlanUseCase;
import com.brightpath.science.util.SciencePreferenceStore;
import com.brightpath.science.util.ScienceUserSession;
import com.brightpath.science.util.ScienceUiFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Generates and tracks a Science schedule beginning at the generation moment. */
public final class ScienceStudyPlannerActivity extends AppCompatActivity {
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

        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
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
            statusView.setText(getString(R.string.science_tool_the_saved_study_plan_could_not_be));
            Toast.makeText(this, getString(R.string.science_tool_unable_to_read_the_saved_study_plan), Toast.LENGTH_SHORT).show();
        }
    }

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindViews`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
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

        /*
 * Responsibility: Updates the state or value handled by `setupSpinners` for ScienceStudyPlannerActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setupSpinners() {
        String[] subjects = new String[ScienceSubject.values().length];
        for (int i = 0; i < subjects.length; i++) subjects[i] = getString(ScienceSubject.values()[i].getDisplayNameResId());
        subjectSpinner.setAdapter(adapter(subjects));

        String[] levels = new String[EducationLevel.values().length];
        for (int i = 0; i < levels.length; i++) levels[i] = getString(EducationLevel.values()[i].getDisplayNameResId());
        levelSpinner.setAdapter(adapter(levels));

        String[] dayLabels = new String[DAY_VALUES.length];
        for (int i = 0; i < dayLabels.length; i++) dayLabels[i] = getString(R.string.science_planner_days_format, DAY_VALUES[i]);
        daysSpinner.setAdapter(adapter(dayLabels));

        String[] minuteLabels = new String[MINUTE_VALUES.length];
        for (int i = 0; i < minuteLabels.length; i++) minuteLabels[i] = getString(R.string.science_planner_minutes_format, MINUTE_VALUES[i]);
        minutesSpinner.setAdapter(adapter(minuteLabels));

        SciencePreferenceStore preferences = new SciencePreferenceStore(this);
        subjectSpinner.setSelection(preferences.getSubject().ordinal());
        levelSpinner.setSelection(preferences.getLevelPosition());
        daysSpinner.setSelection(1);
        minutesSpinner.setSelection(2);
    }

        /*
 * Responsibility: Handles the `adapter` UI interaction/state operation for ScienceStudyPlannerActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private android.widget.ArrayAdapter<String> adapter(String[] values) {
        return ScienceUiFactory.spinnerAdapter(this, values);
    }

        /*
 * Responsibility: Updates the state or value handled by `setupActions` for ScienceStudyPlannerActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setupActions() {
        findViewById(R.id.btnStudyPlannerBack).setOnClickListener(view -> finish());
        generateButton.setOnClickListener(view -> generatePlan());
        clearButton.setOnClickListener(view -> confirmClear());
    }

        /*
 * Responsibility: Generates the Science result handled by `generatePlan` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
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
        statusView.setText(getString(R.string.science_planner_creating, formatDateTime(startMoment)));
        generateUseCase.execute(request, new ResultCallback<StudyPlan>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onSuccess(StudyPlan result) {
                if (destroyed) return;
                setLoading(false);
                showPlan(result);
                Toast.makeText(ScienceStudyPlannerActivity.this,
                        getString(R.string.science_tool_study_plan_created_from_the_current_time), Toast.LENGTH_SHORT).show();
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onError(String message, Throwable error) {
                if (destroyed) return;
                setLoading(false);
                statusView.setText(message == null ? getString(R.string.science_tool_unable_to_create_a_study_plan) : message);
                statusView.setTextColor(getColor(R.color.science_error));
            }
        });
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showPlan` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showPlan(@Nullable StudyPlan plan) {
        activePlan = plan;
        sessionsLayout.removeAllViews();
        clearButton.setVisibility(plan == null ? View.GONE : View.VISIBLE);
        if (plan == null || plan.getSessions().isEmpty()) {
            statusView.setTextColor(getColor(R.color.science_text_secondary));
            statusView.setText(R.string.science_planner_empty);
            return;
        }

        String source = plan.isGeneratedByAi()
                ? getString(R.string.science_planner_source_ai) : getString(R.string.science_planner_source_local);
        statusView.setTextColor(getColor(R.color.science_text_primary));
        statusView.setText(getString(R.string.science_planner_created_label, source, formatDateTime(plan.getCreatedAtMillis()))
                + "\n" + getString(R.string.science_planner_first_session, formatDateTime(plan.getSessions().get(0).getStartAtMillis()))
                + "\n" + getString(plan.getSubject().getDisplayNameResId()) + " · "
                + getString(plan.getLevel().getDisplayNameResId()) + " · " + getString(R.string.science_planner_goal_label, plan.getGoal()));

        for (StudySession session : plan.getSessions()) addSessionView(session);
    }

        /*
 * Responsibility: Adds the data, listener, duration, key, or UI element represented by `addSessionView`.
 * Purpose: Keeps the incremental update explicit and isolated from unrelated responsibilities.
 */
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
        completed.setText(getString(R.string.science_planner_session_item, getString(R.string.science_planner_day_label, session.getDayNumber()), session.getTitle()));
        completed.setTextSize(16f);
        completed.setTypeface(completed.getTypeface(), Typeface.BOLD);
        completed.setChecked(session.isCompleted());
        card.addView(completed);

        TextView time = new TextView(this);
        time.setText(formatDateTime(session.getStartAtMillis())
                + " · " + getString(R.string.science_planner_minutes_format, session.getDurationMinutes()));
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

        /*
 * Responsibility: Adds the data, listener, duration, key, or UI element represented by `addSafeCompletionListener`.
 * Purpose: Keeps the incremental update explicit and isolated from unrelated responsibilities.
 */
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
                Toast.makeText(this, getString(R.string.science_tool_unable_to_save_this_change), Toast.LENGTH_SHORT).show();
            }
        });
    }

        /*
 * Responsibility: Changes the active selection/state handled by `selectedSubject`.
 * Purpose: Keeps the UI and subject-specific Science context synchronized.
 */
private ScienceSubject selectedSubject() {
        int position = subjectSpinner.getSelectedItemPosition();
        if (position == 1) return ScienceSubject.CHEMISTRY;
        if (position == 2) return ScienceSubject.BIOLOGY;
        return ScienceSubject.PHYSICS;
    }

        /*
 * Responsibility: Handles the `confirmClear` UI interaction/state operation for ScienceStudyPlannerActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.science_planner_clear_title)
                .setMessage(R.string.science_planner_clear_msg)
                .setNegativeButton(R.string.science_cancel, null)
                .setPositiveButton(R.string.science_planner_clear_confirm, (dialog, which) -> {
                    try {
                        repository.clear(userId);
                        showPlan(null);
                    } catch (Exception error) {
                        Toast.makeText(this, getString(R.string.science_tool_unable_to_clear_the_saved_plan), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

        /*
 * Responsibility: Updates the state or value handled by `setLoading` for ScienceStudyPlannerActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
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

        /*
 * Responsibility: Releases temporary resources, listeners, or component state during destruction.
 * Purpose: Reduces leaks and prevents stale callbacks from affecting a destroyed Science screen.
 */
@Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `formatDateTime` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String formatDateTime(long millis) {
        return new SimpleDateFormat("EEE, dd MMM yyyy · HH:mm", Locale.getDefault())
                .format(new Date(millis));
    }

        /*
 * Responsibility: Handles the `dp` UI interaction/state operation for ScienceStudyPlannerActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
