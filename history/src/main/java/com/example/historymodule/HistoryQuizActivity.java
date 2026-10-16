package com.example.historymodule;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.historymodule.api.HistoryAiService;
import com.example.historymodule.databases.SqliteDbHelper;
import com.example.historymodule.model.HistoryLevelCalculator;
import com.example.historymodule.model.HistoryQuizQuestion;
import com.example.historymodule.util.HistoryUserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * Role: Activity for History, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public final class HistoryQuizActivity extends AppCompatActivity {
    public static final String EXTRA_TOPIC = "history_quiz_topic";
    public static final String EXTRA_EDUCATION_LEVEL = "history_quiz_education_level";

    private TextView titleView;
    private TextView educationLevelView;
    private TextView statusView;
    private TextView counterView;
    private TextView difficultyView;
    private TextView questionView;
    private TextView explanationView;
    private TextView expStatusView;
    private RadioGroup optionGroup;
    private final RadioButton[] optionButtons = new RadioButton[4];
    private Button actionButton;
    private ProgressBar loadingView;
    private View quizContent;

    private final List<HistoryQuizQuestion> questions = new ArrayList<>();
    private HistoryAiService aiService;
    private SqliteDbHelper dbHelper;
    private String topic;
    private String educationLevel;
    private int userId;
    private int currentIndex;
    private int correctCount;
    private int earnedExp;
    private boolean checkedCurrentQuestion;
    private boolean finished;

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity_quiz);

        userId = HistoryUserSession.getUserId(this);
        aiService = new HistoryAiService();
        dbHelper = new SqliteDbHelper(this);
        topic = safeExtra(EXTRA_TOPIC, "Tổng hợp");
        educationLevel = safeExtra(EXTRA_EDUCATION_LEVEL, "Trung học cơ sở");

        bindViews();
        setupActions();
        generateQuiz();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            /*
             * Responsibility: Handles the `handle on back pressed` workflow or event and coordinates its related steps.
             * Purpose: A dedicated coordinator method makes the processing order clear and simplifies debugging and explanation.
             */
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
    }

    /*
     * Responsibility: Performs the `safe extra` responsibility of `HistoryQuizActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String safeExtra(String key, String fallback) {
        String value = getIntent() == null ? null : getIntent().getStringExtra(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    /*
     * Responsibility: Displays or binds data from `bind views` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void bindViews() {
        titleView = findViewById(R.id.tvHistoryQuizTitle);
        educationLevelView = findViewById(R.id.tvHistoryQuizEducationLevel);
        statusView = findViewById(R.id.tvHistoryQuizStatus);
        counterView = findViewById(R.id.tvHistoryQuizCounter);
        difficultyView = findViewById(R.id.tvHistoryQuizDifficulty);
        questionView = findViewById(R.id.tvHistoryQuizQuestion);
        explanationView = findViewById(R.id.tvHistoryQuizExplanation);
        expStatusView = findViewById(R.id.tvHistoryQuizExpStatus);
        optionGroup = findViewById(R.id.rgHistoryQuizOptions);
        optionButtons[0] = findViewById(R.id.rbHistoryOptionA);
        optionButtons[1] = findViewById(R.id.rbHistoryOptionB);
        optionButtons[2] = findViewById(R.id.rbHistoryOptionC);
        optionButtons[3] = findViewById(R.id.rbHistoryOptionD);
        actionButton = findViewById(R.id.btnHistoryQuizAction);
        loadingView = findViewById(R.id.pbHistoryQuizLoading);
        quizContent = findViewById(R.id.historyQuizContent);
        titleView.setText(topic);
        educationLevelView.setText(getString(R.string.history_quiz_education_label, educationLevel));
    }

    /*
     * Responsibility: Configures `setup actions` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupActions() {
        findViewById(R.id.btnHistoryQuizBack).setOnClickListener(v -> handleBack());
        actionButton.setOnClickListener(v -> {
            if (checkedCurrentQuestion) moveToNextQuestion();
            else checkCurrentAnswer();
        });
    }

    /*
     * Responsibility: Handles the `handle back` workflow or event and coordinates its related steps.
     * Purpose: A dedicated coordinator method makes the processing order clear and simplifies debugging and explanation.
     */
    private void handleBack() {
        if (!questions.isEmpty() && !finished && currentIndex < questions.size()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.history_quiz_exit_title)
                    .setMessage(R.string.history_quiz_exit_msg)
                    .setPositiveButton(R.string.history_quiz_exit_confirm, (dialog, which) -> finish())
                    .setNegativeButton(R.string.history_quiz_exit_continue, null)
                    .show();
        } else {
            finish();
        }
    }

    /*
     * Responsibility: Generates data or content for `generate quiz` based on the input and module rules.
     * Purpose: Keeping content generation separate makes the strategy easier to change without affecting presentation logic.
     */
    private void generateQuiz() {
        finished = false;
        showLoading(getString(R.string.history_quiz_ai_generating, topic, educationLevel));
        aiService.generateQuiz(topic, educationLevel, 5,
                new HistoryAiService.Callback<List<HistoryQuizQuestion>>() {
                    @Override
                    public void onSuccess(List<HistoryQuizQuestion> result) {
                        questions.clear();
                        questions.addAll(result);
                        currentIndex = 0;
                        correctCount = 0;
                        earnedExp = 0;
                        showQuizContent();
                        showQuestion();
                    }

                    /*
                     * Responsibility: Handles the Android callback or event `onError` and updates the related state.
                     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
                     */
                    @Override
                    public void onError(String message, Throwable error) {
                        loadingView.setVisibility(View.GONE);
                        statusView.setVisibility(View.VISIBLE);
                        statusView.setText(message);
                        actionButton.setVisibility(View.VISIBLE);
                        actionButton.setText(R.string.history_quiz_retry);
                        actionButton.setOnClickListener(v -> generateQuiz());
                    }
                });
    }

    /*
     * Responsibility: Displays or binds data from `show loading` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showLoading(String message) {
        loadingView.setVisibility(View.VISIBLE);
        statusView.setVisibility(View.VISIBLE);
        statusView.setText(message);
        quizContent.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);
    }

    /*
     * Responsibility: Displays or binds data from `show quiz content` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showQuizContent() {
        loadingView.setVisibility(View.GONE);
        statusView.setVisibility(View.GONE);
        quizContent.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setOnClickListener(v -> {
            if (checkedCurrentQuestion) moveToNextQuestion();
            else checkCurrentAnswer();
        });
    }

    /*
     * Responsibility: Displays or binds data from `show question` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showQuestion() {
        if (currentIndex < 0 || currentIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        checkedCurrentQuestion = false;
        HistoryQuizQuestion question = questions.get(currentIndex);
        counterView.setText(getString(R.string.history_quiz_counter_format, currentIndex + 1, questions.size()));
        difficultyView.setText(getString(R.string.history_quiz_difficulty_reward, question.getDifficultyDisplayName(), question.getExpReward()));
        expStatusView.setText(getString(R.string.history_quiz_current_earned, earnedExp));
        questionView.setText(question.getQuestion());
        explanationView.setText("");
        explanationView.setVisibility(View.GONE);
        optionGroup.clearCheck();

        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(optionLabel(i) + ". " + question.getOptions().get(i));
            optionButtons[i].setEnabled(true);
        }
        actionButton.setText(R.string.history_quiz_check_answer);
    }

    /*
     * Responsibility: Validates the conditions or rules handled by `check current answer` and returns the result to the caller.
     * Purpose: A dedicated validation method keeps rules consistent and easier to test.
     */
    private void checkCurrentAnswer() {
        int checkedId = optionGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, R.string.history_quiz_select_option, Toast.LENGTH_SHORT).show();
            return;
        }

        HistoryQuizQuestion question = questions.get(currentIndex);
        int selectedIndex = selectedOptionIndex(checkedId);
        boolean correct = selectedIndex == question.getCorrectIndex();
        int gained = 0;
        if (correct) {
            correctCount++;
            gained = question.getExpReward();
            earnedExp += gained;
        }

        checkedCurrentQuestion = true;
        for (RadioButton button : optionButtons) button.setEnabled(false);

        String resultLine = correct
                ? getString(R.string.history_quiz_correct_msg, gained)
                : getString(R.string.history_quiz_wrong_msg, optionLabel(question.getCorrectIndex()));
        explanationView.setText(resultLine + "\n\n" + question.getExplanation());
        explanationView.setVisibility(View.VISIBLE);
        expStatusView.setText(getString(R.string.history_quiz_current_earned, earnedExp));
        actionButton.setText(currentIndex + 1 >= questions.size()
                ? R.string.history_quiz_view_result : R.string.history_quiz_next_question);
    }

    /*
     * Responsibility: Performs the `move to next question` responsibility of `HistoryQuizActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void moveToNextQuestion() {
        currentIndex++;
        if (currentIndex >= questions.size()) finishQuiz();
        else showQuestion();
    }

    /*
     * Responsibility: Performs the `finish quiz` responsibility of `HistoryQuizActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void finishQuiz() {
        if (finished) return;
        finished = true;
        int oldTotalExp = dbHelper.getTotalQuizExp(userId);
        long saved = dbHelper.saveQuizAttempt(
                userId, topic, educationLevel, correctCount, questions.size(), earnedExp);
        int newTotalExp = saved > 0 ? dbHelper.getTotalQuizExp(userId) : oldTotalExp;
        int percent = questions.isEmpty() ? 0
                : Math.round(correctCount * 100f / questions.size());
        int oldLevel = HistoryLevelCalculator.getLevel(oldTotalExp);
        int newLevel = HistoryLevelCalculator.getLevel(newTotalExp);

        StringBuilder message = new StringBuilder()
                .append(getString(R.string.history_quiz_finish_scope, topic))
                .append("\n").append(getString(R.string.history_quiz_finish_level, educationLevel))
                .append("\n").append(getString(R.string.history_quiz_finish_result, correctCount, questions.size()))
                .append("\n").append(getString(R.string.history_quiz_finish_accuracy, percent))
                .append("\n").append(getString(R.string.history_quiz_finish_earned, saved > 0 ? earnedExp : 0))
                .append("\n").append(getString(R.string.history_quiz_finish_total, newTotalExp))
                .append("\n").append(getString(R.string.history_quiz_finish_level_label, newLevel, getString(HistoryLevelCalculator.getTitleResId(newLevel))));
        if (saved <= 0) message.append("\n\n").append(getString(R.string.history_quiz_finish_error));
        if (newLevel > oldLevel) message.append("\n\n").append(getString(R.string.history_quiz_finish_levelup));

        new AlertDialog.Builder(this)
                .setTitle(R.string.history_quiz_finish_title)
                .setMessage(message.toString())
                .setPositiveButton(R.string.history_quiz_btn_new, (dialog, which) -> generateQuiz())
                .setNegativeButton(R.string.history_quiz_btn_close, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    /*
     * Responsibility: Performs the `selected option index` responsibility of `HistoryQuizActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private int selectedOptionIndex(int checkedId) {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].getId() == checkedId) return i;
        }
        return -1;
    }

    /*
     * Responsibility: Performs the `option label` responsibility of `HistoryQuizActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private String optionLabel(int index) {
        return String.valueOf((char) ('A' + Math.max(0, Math.min(3, index))));
    }

    @Override
    protected void onDestroy() {
        if (aiService != null) aiService.shutdown();
        if (dbHelper != null) dbHelper.close();
        super.onDestroy();
    }
}
