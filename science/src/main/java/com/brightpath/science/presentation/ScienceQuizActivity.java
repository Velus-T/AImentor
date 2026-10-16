package com.brightpath.science.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.brightpath.science.R;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.GeneratedQuiz;
import com.brightpath.science.domain.model.QuizQuestion;
import com.brightpath.science.domain.model.QuizRequest;
import com.brightpath.science.domain.model.ScienceSubject;
import com.brightpath.science.domain.repository.QuizAttemptRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.domain.usecase.GenerateScienceQuizUseCase;
import com.brightpath.science.presentation.view.QuizQuestionView;
import com.brightpath.science.util.ScienceLevelCalculator;
import com.brightpath.science.util.SciencePreferenceStore;
import com.brightpath.science.util.ScienceUserSession;
import com.brightpath.science.util.ScienceUiFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ScienceQuizActivity extends AppCompatActivity {
    private Spinner subjectSpinner;
    private Spinner levelSpinner;
    private Spinner countSpinner;
    private EditText topicInput;
    private LinearLayout quizContainer;
    private ProgressBar progressBar;
    private Button generateButton;
    private Button submitButton;
    private TextView resultView;
    private TextView adaptiveStatusView;
    private TextView totalExpView;
    private TextView levelTitleView;
    private TextView levelProgressTextView;
    private ProgressBar levelProgressBar;

    private GenerateScienceQuizUseCase generateQuizUseCase;
    private QuizAttemptRepository attemptRepository;
    private int userId;
    private QuizRequest activeQuizRequest;
    private List<QuizQuestion> activeQuestions = new ArrayList<>();
    private final List<QuizQuestionView> questionViews = new ArrayList<>();
    private boolean loading;
    private volatile boolean destroyed;

        /*
 * Responsibility: Initializes the Android component, inflates/binds its layout, and prepares dependencies/listeners/state.
 * Purpose: Establishes the screen's initial state before the learner starts interacting with it.
 */
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_science_quiz);

        ScienceModule module = ScienceModule.get(this);
        generateQuizUseCase = module.generateScienceQuizUseCase();
        attemptRepository = module.quizAttemptRepository();
        userId = new ScienceUserSession(this).getUserId();

        bindViews();
        setupSpinners();
        updateTotalExpDisplay();
        findViewById(R.id.btnQuizBack).setOnClickListener(view -> finish());
        generateButton.setOnClickListener(view -> generateQuiz());
        submitButton.setOnClickListener(view -> submitQuiz());

        Object retained = getLastCustomNonConfigurationInstance();
        if (retained instanceof RetainedQuiz) {
            RetainedQuiz state = (RetainedQuiz) retained;
            activeQuizRequest = state.request;
            activeQuestions = state.questions;
            restoreQuizInputs(activeQuizRequest);
            renderQuiz(activeQuestions);
            for (int i = 0; i < questionViews.size() && i < state.answers.size(); i++) {
                questionViews.get(i).restoreAnswer(state.answers.get(i));
                if (state.submitted) questionViews.get(i).showFeedback();
            }
            if (state.submitted) {
                resultView.setText(state.resultText);
                resultView.setVisibility(View.VISIBLE);
                submitButton.setEnabled(false);
            }
            showAdaptiveStatus(activeQuizRequest);
            generateButton.setText("Generate a different quiz");
        }
    }

        /*
 * Responsibility: Binds the relevant data, view, or listener in `bindViews`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bindViews() {
        subjectSpinner = findViewById(R.id.spQuizSubject);
        levelSpinner = findViewById(R.id.spQuizLevel);
        countSpinner = findViewById(R.id.spQuizCount);
        topicInput = findViewById(R.id.etQuizTopic);
        quizContainer = findViewById(R.id.quizContainer);
        progressBar = findViewById(R.id.progressQuiz);
        generateButton = findViewById(R.id.btnGenerateQuiz);
        submitButton = findViewById(R.id.btnSubmitQuiz);
        resultView = findViewById(R.id.tvQuizResult);
        adaptiveStatusView = findViewById(R.id.tvQuizAdaptiveStatus);
        totalExpView = findViewById(R.id.tvScienceTotalExp);
        levelTitleView = findViewById(R.id.tvScienceLevelTitle);
        levelProgressTextView = findViewById(R.id.tvScienceLevelProgress);
        levelProgressBar = findViewById(R.id.progressScienceLevel);
    }

        /*
 * Responsibility: Updates the state or value handled by `setupSpinners` for ScienceQuizActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setupSpinners() {
        String[] subjects = new String[ScienceSubject.values().length];
        for (int i = 0; i < subjects.length; i++) subjects[i] = getString(ScienceSubject.values()[i].getDisplayNameResId());
        subjectSpinner.setAdapter(adapter(subjects));

        String[] levels = new String[EducationLevel.values().length];
        for (int i = 0; i < levels.length; i++) levels[i] = getString(EducationLevel.values()[i].getDisplayNameResId());
        levelSpinner.setAdapter(adapter(levels));
        
        String[] counts = new String[]{
                getString(R.string.science_questions_count, 3),
                getString(R.string.science_questions_count, 4),
                getString(R.string.science_questions_count, 5)
        };
        countSpinner.setAdapter(adapter(counts));

        SciencePreferenceStore preferences = new SciencePreferenceStore(this);
        subjectSpinner.setSelection(preferences.getSubject().ordinal());
        levelSpinner.setSelection(preferences.getLevelPosition());
        countSpinner.setSelection(2);
    }

        /*
 * Responsibility: Handles the `adapter` UI interaction/state operation for ScienceQuizActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private android.widget.ArrayAdapter<String> adapter(String[] values) {
        return ScienceUiFactory.spinnerAdapter(this, values);
    }


        /*
 * Responsibility: Restores the previously saved state handled by `restoreQuizInputs`.
 * Purpose: Allows the learner to continue the same Science flow after navigation, rotation, or reopening saved content.
 */
private void restoreQuizInputs(QuizRequest request) {
        if (request == null) return;
        subjectSpinner.setSelection(request.getSubject().ordinal());
        levelSpinner.setSelection(request.getLevel().ordinal());
        countSpinner.setSelection(Math.max(0, Math.min(2, request.getNumberOfQuestions() - 3)));
        topicInput.setText(request.getTopic());
    }

        /*
 * Responsibility: Generates the Science result handled by `generateQuiz` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
private void generateQuiz() {
        if (loading) return;
        setLoading(true);
        resultView.setVisibility(View.GONE);
        adaptiveStatusView.setText("AI is creating a new level-appropriate variation…");
        adaptiveStatusView.setTextColor(getColor(R.color.science_text_secondary));

        QuizRequest baseRequest = new QuizRequest(
                selectedSubject(),
                EducationLevel.fromPosition(levelSpinner.getSelectedItemPosition()),
                topicInput.getText().toString(),
                countSpinner.getSelectedItemPosition() + 3
        );

        generateQuizUseCase.execute(userId, baseRequest, new ResultCallback<GeneratedQuiz>() {
                        /*
 * Responsibility: Performs the `onSuccess` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onSuccess(GeneratedQuiz generatedQuiz) {
                if (destroyed) return;
                setLoading(false);
                activeQuizRequest = generatedQuiz.getRequest();
                activeQuestions = new ArrayList<>(generatedQuiz.getQuestions());
                renderQuiz(activeQuestions);
                showAdaptiveStatus(activeQuizRequest);
                generateButton.setText("Generate a different quiz");
            }

                        /*
 * Responsibility: Performs the `onError` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
            public void onError(String message, Throwable error) {
                if (destroyed) return;
                setLoading(false);
                adaptiveStatusView.setText("Quiz generation failed. Your selected level was not changed.");
                adaptiveStatusView.setTextColor(getColor(R.color.science_error));
                Toast.makeText(ScienceQuizActivity.this,
                        message == null ? "Unable to generate a quiz." : message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `renderQuiz` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void renderQuiz(List<QuizQuestion> questions) {
        questionViews.clear();
        quizContainer.removeAllViews();
        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestionView view = new QuizQuestionView(this, questions.get(i), i + 1);
                questionViews.add(view);
                quizContainer.addView(view);
            }
        }
        submitButton.setVisibility(questionViews.isEmpty() ? View.GONE : View.VISIBLE);
        submitButton.setEnabled(!questionViews.isEmpty());
        resultView.setVisibility(View.GONE);
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showAdaptiveStatus` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showAdaptiveStatus(QuizRequest request) {
        if (request == null) return;
        double accuracy = request.getRecentPerformance().getAccuracyPercent();
        String performance = accuracy < 0
                ? "first quiz at this level"
                : String.format(Locale.getDefault(), "recent accuracy %.0f%%", accuracy);
        adaptiveStatusView.setTextColor(getColor(R.color.science_text_secondary));
        adaptiveStatusView.setText(request.getLevel().getDisplayName()
                + " • " + request.getChallenge().getDisplayName()
                + " • " + performance
                + "\nA new variant was generated; recent questions were excluded.");
    }

        /*
 * Responsibility: Handles the `submitQuiz` UI interaction/state operation for ScienceQuizActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private void submitQuiz() {
        if (activeQuizRequest == null || questionViews.isEmpty()) return;
        for (int i = 0; i < questionViews.size(); i++) {
            if (!questionViews.get(i).hasAnswer()) {
                Toast.makeText(this, "Hãy trả lời câu " + (i + 1) + " trước khi nộp.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int correct = 0;
        int expEarned = 0;
        for (QuizQuestionView questionView : questionViews) {
            if (questionView.isCorrect()) {
                correct++;
                expEarned += questionView.getExpReward();
            }
            questionView.showFeedback();
        }

        int previousTotalExp = safeTotalExp();
        int previousLevel = ScienceLevelCalculator.getLevel(previousTotalExp);

        boolean saved = false;
        try {
            long attemptId = attemptRepository.saveAttempt(
                    userId,
                    activeQuizRequest.getSubject(),
                    activeQuizRequest.getLevel(),
                    activeQuizRequest.getTopic(),
                    activeQuizRequest.getChallenge(),
                    questionViews.size(),
                    correct,
                    expEarned
            );
            saved = attemptId != -1L;
        } catch (Exception error) {
            Toast.makeText(this,
                    "Đã chấm điểm nhưng chưa thể lưu tiến độ và EXP.",
                    Toast.LENGTH_SHORT).show();
        }

        double percentage = correct * 100.0 / questionViews.size();
        String nextMode;
        if (percentage < 50) {
            nextMode = "Quiz tiếp theo sẽ tăng gợi ý nhưng vẫn giữ đúng cấp độ đã chọn.";
        } else if (percentage >= 80) {
            nextMode = "Quiz tiếp theo sẽ tăng độ thử thách trong cùng cấp độ.";
        } else {
            nextMode = "Quiz tiếp theo sẽ giữ mức hiện tại và đổi bộ câu hỏi.";
        }

        int totalExp = safeTotalExp();
        int currentLevel = ScienceLevelCalculator.getLevel(totalExp);
        String expLine = saved
                ? getString(R.string.science_quiz_exp_saved, expEarned, totalExp)
                : getString(R.string.science_quiz_exp_not_saved, expEarned);
        
        String levelName = getString(ScienceLevelCalculator.getTitleResId(currentLevel));
        String progressInfo = currentLevel >= ScienceLevelCalculator.MAX_LEVEL
                ? getString(R.string.science_level_max)
                : getString(R.string.science_level_remaining_format, ScienceLevelCalculator.getRemainingExp(totalExp));
        
        String levelLine = getString(R.string.science_quiz_level_line,
                getString(R.string.science_level_format, currentLevel),
                levelName,
                progressInfo);

        resultView.setText(getString(R.string.science_quiz_result_format,
                correct, questionViews.size(), percentage,
                expLine, levelLine, nextMode));
        resultView.setVisibility(View.VISIBLE);
        resultView.announceForAccessibility(resultView.getText());
        submitButton.setEnabled(false);
        updateTotalExpDisplay();

        if (saved && currentLevel > previousLevel) {
            showLevelUpDialog(previousLevel, currentLevel, totalExp);
        }
    }

        /*
 * Responsibility: Updates the state or value handled by `updateTotalExpDisplay` for ScienceQuizActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void updateTotalExpDisplay() {
        int totalExp = safeTotalExp();
        int level = ScienceLevelCalculator.getLevel(totalExp);

        if (totalExpView != null) {
            totalExpView.setText(getString(R.string.science_total_exp_format, totalExp));
        }
        if (levelTitleView != null) {
            levelTitleView.setText(getString(R.string.science_level_format, level) + " • "
                    + getString(ScienceLevelCalculator.getTitleResId(level)));
        }
        if (levelProgressBar != null) {
            levelProgressBar.setProgress(
                    ScienceLevelCalculator.getProgressPercent(totalExp));
        }
        if (levelProgressTextView != null) {
            if (level >= ScienceLevelCalculator.MAX_LEVEL) {
                levelProgressTextView.setText(getString(R.string.science_level_max) + " • 6.555 EXP");
            } else {
                levelProgressTextView.setText(getString(R.string.science_level_exp_format,
                                ScienceLevelCalculator.getExpInsideCurrentLevel(totalExp),
                                ScienceLevelCalculator.getExpToNextLevel(level))
                                + " • "
                                + getString(R.string.science_level_remaining_format,
                                ScienceLevelCalculator.getRemainingExp(totalExp)));
            }
        }
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showLevelUpDialog` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
private void showLevelUpDialog(int oldLevel, int newLevel, int totalExp) {
        String jumpedLevels = newLevel - oldLevel > 1
                ? getString(R.string.science_levelup_jumped, newLevel - oldLevel)
                : "";

        new AlertDialog.Builder(this)
                .setTitle(R.string.science_levelup_title)
                .setMessage(jumpedLevels
                        + getString(R.string.science_levelup_msg, newLevel,
                        getString(ScienceLevelCalculator.getTitleResId(newLevel)), totalExp))
                .setPositiveButton(R.string.science_continue, null)
                .show();
    }

        /*
 * Responsibility: Handles the `safeTotalExp` UI interaction/state operation for ScienceQuizActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
private int safeTotalExp() {
        try {
            return attemptRepository == null ? 0 : attemptRepository.getTotalExp(userId);
        } catch (Exception ignored) {
            return 0;
        }
    }

        /*
 * Responsibility: Refreshes the current state/data when the component returns to the foreground.
 * Purpose: Ensures the UI reflects changes made on other Science screens.
 */
@Override
    protected void onResume() {
        super.onResume();
        updateTotalExpDisplay();
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
 * Responsibility: Updates the state or value handled by `setLoading` for ScienceQuizActivity.
 * Purpose: Keeps the component synchronized with the latest learner, UI, or domain state.
 */
private void setLoading(boolean value) {
        loading = value;
        progressBar.setVisibility(value ? View.VISIBLE : View.GONE);
        generateButton.setEnabled(!value);
        submitButton.setEnabled(!value && !questionViews.isEmpty());
        subjectSpinner.setEnabled(!value);
        levelSpinner.setEnabled(!value);
        countSpinner.setEnabled(!value);
        topicInput.setEnabled(!value);
    }

        /*
 * Responsibility: Handles the `onRetainCustomNonConfigurationInstance` UI interaction/state operation for ScienceQuizActivity.
 * Purpose: Keeps this screen responsibility in a focused function so the main lifecycle code remains readable and easy to explain.
 */
@Nullable
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (loading || activeQuizRequest == null || activeQuestions.isEmpty()) return null;
        List<String> answers = new ArrayList<>();
        for (QuizQuestionView view : questionViews) answers.add(view.getUserAnswer());
        boolean submitted = !submitButton.isEnabled() && resultView.getVisibility() == View.VISIBLE;
        return new RetainedQuiz(activeQuizRequest, new ArrayList<>(activeQuestions),
                answers, submitted, String.valueOf(resultView.getText()));
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

    private static final class RetainedQuiz {
        private final QuizRequest request;
        private final List<QuizQuestion> questions;
        private final List<String> answers;
        private final boolean submitted;
        private final String resultText;

                /*
 * Responsibility: Initializes RetainedQuiz and receives or prepares the dependencies/state required by this Science Module component.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private RetainedQuiz(QuizRequest request,
                             List<QuizQuestion> questions,
                             List<String> answers,
                             boolean submitted,
                             String resultText) {
            this.request = request;
            this.questions = questions;
            this.answers = answers;
            this.submitted = submitted;
            this.resultText = resultText;
        }
    }
}
