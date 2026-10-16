package com.example.mathmodule;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.mathmodule.databases.QuizQuestion;
import com.example.mathmodule.databases.SqliteDbHelper;
import com.example.mathmodule.util.MathLevelCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * Role: Activity for Math, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public class QuizPlayActivity extends AppCompatActivity {

    private static final String STATE_INDEX = "math_quiz_index";
    private static final String STATE_TIME = "math_quiz_time";
    private static final String STATE_ANSWERS = "math_quiz_answers";
    private static final String STATE_SUBMITTED = "math_quiz_submitted";
    private static final String STATE_EXPLANATION = "math_quiz_explanation";
    private static final String STATE_SCORE = "math_quiz_score";
    private static final String STATE_EXP = "math_quiz_exp";

    private List<QuizQuestion> questions;
    private int currentQuestionIndex;
    private int score;
    private int expEarned;
    private int setId;
    private SqliteDbHelper dbHelper;

    private TextView txtQuizTitle;
    private TextView txtTimer;
    private TextView txtQuestionNumber;
    private TextView txtQuestionDifficulty;
    private WebView webQuestion;
    private WebView webOptA;
    private WebView webOptB;
    private WebView webOptC;
    private WebView webOptD;
    private ProgressBar quizProgress;
    private CardView cardOptA;
    private CardView cardOptB;
    private CardView cardOptC;
    private CardView cardOptD;
    private Button btnPrev;
    private Button btnNext;
    private ImageButton btnBack;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isExplanationMode;
    private boolean quizSubmitted;

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_play);

        setId = getIntent().getIntExtra("QUIZ_SET_ID", -1);
        String setTitle = getIntent().getStringExtra("QUIZ_SET_TITLE");
        dbHelper = new SqliteDbHelper(this);
        questions = dbHelper.getQuestionsBySetId(setId);

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy câu hỏi trong bộ đề này!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        txtQuizTitle.setText(setTitle == null || setTitle.trim().isEmpty()
                ? "Đang làm bài" : setTitle.trim());

        if (savedInstanceState == null) {
            timeLeftInMillis = Math.max(5, Math.min(30, questions.size())) * 60_000L;
        } else {
            restoreState(savedInstanceState);
        }

        setupOptionClickListeners();
        btnBack.setOnClickListener(v -> handleExit());
        btnNext.setOnClickListener(v -> handleNext());
        btnPrev.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion();
            }
        });

        displayQuestion();
        if (!quizSubmitted && !isExplanationMode) {
            startTimer();
        } else {
            updateTimerText();
        }

        if (savedInstanceState != null && quizSubmitted && !isExplanationMode) {
            showResultDialog();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            /*
             * Responsibility: Handles the `handle on back pressed` workflow or event and coordinates its related steps.
             * Purpose: A dedicated coordinator method makes the processing order clear and simplifies debugging and explanation.
             */
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });
    }

    /*
     * Responsibility: Configures `init ui` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void initUI() {
        txtQuizTitle = findViewById(R.id.txt_quiz_title);
        txtTimer = findViewById(R.id.txt_timer);
        txtQuestionNumber = findViewById(R.id.txt_question_number);
        txtQuestionDifficulty = findViewById(R.id.txt_question_difficulty);
        webQuestion = findViewById(R.id.web_question);
        quizProgress = findViewById(R.id.quiz_progress);
        cardOptA = findViewById(R.id.card_opt_a);
        cardOptB = findViewById(R.id.card_opt_b);
        cardOptC = findViewById(R.id.card_opt_c);
        cardOptD = findViewById(R.id.card_opt_d);
        webOptA = findViewById(R.id.web_opt_a);
        webOptB = findViewById(R.id.web_opt_b);
        webOptC = findViewById(R.id.web_opt_c);
        webOptD = findViewById(R.id.web_opt_d);
        btnPrev = findViewById(R.id.btn_prev_question);
        btnNext = findViewById(R.id.btn_next_question);
        btnBack = findViewById(R.id.btn_back_quiz);

        setupWebView(webQuestion);
        setupWebView(webOptA);
        setupWebView(webOptB);
        setupWebView(webOptC);
        setupWebView(webOptD);
    }

    /*
     * Responsibility: Configures `setup web view` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
    }

    /*
     * Responsibility: Displays or binds data from `display question` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void displayQuestion() {
        QuizQuestion question = questions.get(currentQuestionIndex);
        txtQuestionNumber.setText(String.format(
                Locale.getDefault(),
                "Câu hỏi %d/%d",
                currentQuestionIndex + 1,
                questions.size()
        ));
        txtQuestionDifficulty.setText(
                question.getDifficultyDisplayName()
                        + " • +" + question.getExpReward() + " EXP nếu đúng"
        );
        quizProgress.setMax(questions.size());
        quizProgress.setProgress(currentQuestionIndex + 1);

        String questionDisplay = question.getQuestionText();
        if (isExplanationMode) {
            boolean correct = question.getSelectedAnswer().equals(question.getCorrectAnswer());
            questionDisplay += "<br><br><div style='padding:12px;background:#E8F5E9;"
                    + "border-radius:8px;border-left:4px solid #4CAF50;'>"
                    + "<b>Lời giải chi tiết:</b><br>" + question.getExplanation()
                    + "<br><br><b>EXP câu này:</b> +"
                    + (correct ? question.getExpReward() : 0) + " EXP</div>";
        }
        renderMathText(questionDisplay, webQuestion);
        renderMathText("A. " + question.getOptionA(), webOptA);
        renderMathText("B. " + question.getOptionB(), webOptB);
        renderMathText("C. " + question.getOptionC(), webOptC);
        renderMathText("D. " + question.getOptionD(), webOptD);

        resetOptionBackgrounds();
        if (isExplanationMode) {
            highlightExplanations(question);
        } else {
            highlightSelectedOption(question.getSelectedAnswer());
        }

        btnPrev.setEnabled(currentQuestionIndex > 0);
        if (currentQuestionIndex == questions.size() - 1) {
            btnNext.setText(isExplanationMode ? "Thoát" : "Nộp bài");
        } else {
            btnNext.setText("Câu tiếp");
        }
    }

    /*
     * Responsibility: Handles the `handle next` workflow or event and coordinates its related steps.
     * Purpose: A dedicated coordinator method makes the processing order clear and simplifies debugging and explanation.
     */
    private void handleNext() {
        if (isExplanationMode && currentQuestionIndex == questions.size() - 1) {
            finish();
            return;
        }
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayQuestion();
        } else if (!quizSubmitted) {
            showFinishConfirmation();
        } else {
            finish();
        }
    }

    /*
     * Responsibility: Performs the `highlight explanations` responsibility of `QuizPlayActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void highlightExplanations(QuizQuestion question) {
        String selected = question.getSelectedAnswer();
        String correct = question.getCorrectAnswer();
        markOption(correct, "#C8E6C9");
        if (!selected.isEmpty() && !selected.equals(correct)) {
            markOption(selected, "#FFCDD2");
        }
    }

    /*
     * Responsibility: Performs the `mark option` responsibility of `QuizPlayActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void markOption(String option, String color) {
        switch (option) {
            case "A": cardOptA.setCardBackgroundColor(Color.parseColor(color)); break;
            case "B": cardOptB.setCardBackgroundColor(Color.parseColor(color)); break;
            case "C": cardOptC.setCardBackgroundColor(Color.parseColor(color)); break;
            case "D": cardOptD.setCardBackgroundColor(Color.parseColor(color)); break;
            default: break;
        }
    }

    /*
     * Responsibility: Displays or binds data from `render math text` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void renderMathText(String text, WebView webView) {
        String htmlContent = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css\">"
                + "<script defer src=\"https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js\"></script>"
                + "<script defer src=\"https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js\" "
                + "onload=\"renderMathInElement(document.body,{delimiters:[{left:'$$',right:'$$',display:true},{left:'$',right:'$',display:false}],throwOnError:false});\"></script>"
                + "<style>body{font-family:sans-serif;font-size:16px;color:#263238;padding:0;margin:0;"
                + "word-wrap:break-word;white-space:pre-wrap;background:transparent;}</style>"
                + "</head><body>" + text + "</body></html>";
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    /*
     * Responsibility: Configures `setup option click listeners` with the required Views, listeners, or dependencies.
     * Purpose: Separating configuration from the main flow keeps initialization concise and easier to verify.
     */
    private void setupOptionClickListeners() {
        View.OnTouchListener touchListener = (view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (view.getId() == R.id.web_opt_a) selectOption("A");
                else if (view.getId() == R.id.web_opt_b) selectOption("B");
                else if (view.getId() == R.id.web_opt_c) selectOption("C");
                else if (view.getId() == R.id.web_opt_d) selectOption("D");
                view.performClick();
            }
            return false;
        };
        webOptA.setOnTouchListener(touchListener);
        webOptB.setOnTouchListener(touchListener);
        webOptC.setOnTouchListener(touchListener);
        webOptD.setOnTouchListener(touchListener);
        cardOptA.setOnClickListener(v -> selectOption("A"));
        cardOptB.setOnClickListener(v -> selectOption("B"));
        cardOptC.setOnClickListener(v -> selectOption("C"));
        cardOptD.setOnClickListener(v -> selectOption("D"));
    }

    /*
     * Responsibility: Performs the `select option` responsibility of `QuizPlayActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void selectOption(String option) {
        if (isExplanationMode || quizSubmitted) return;
        questions.get(currentQuestionIndex).setSelectedAnswer(option);
        highlightSelectedOption(option);
    }

    /*
     * Responsibility: Clears or resets the data and state related to `reset option backgrounds`.
     * Purpose: Keeping deletion logic separate helps ensure related UI and data updates are not missed.
     */
    private void resetOptionBackgrounds() {
        cardOptA.setCardBackgroundColor(Color.WHITE);
        cardOptB.setCardBackgroundColor(Color.WHITE);
        cardOptC.setCardBackgroundColor(Color.WHITE);
        cardOptD.setCardBackgroundColor(Color.WHITE);
    }

    /*
     * Responsibility: Performs the `highlight selected option` responsibility of `QuizPlayActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void highlightSelectedOption(String option) {
        resetOptionBackgrounds();
        markOption(option, "#F3E5F5");
    }

    /*
     * Responsibility: Navigates to or starts the `start timer` flow and passes the required data to the next step.
     * Purpose: Centralizing navigation makes Intents, Fragments, and parameters between screens easier to control.
     */
    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            /*
             * Responsibility: Handles the Android callback or event `onFinish` and updates the related state.
             * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
             */
            @Override
            public void onFinish() {
                timeLeftInMillis = 0L;
                updateTimerText();
                finishQuiz();
            }
        }.start();
    }

    /*
     * Responsibility: Updates or synchronizes `update timer text` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000L) / 60;
        int seconds = (int) (timeLeftInMillis / 1000L) % 60;
        txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    /*
     * Responsibility: Displays or binds data from `show finish confirmation` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showFinishConfirmation() {
        int unanswered = 0;
        for (QuizQuestion question : questions) {
            if (question.getSelectedAnswer().isEmpty()) unanswered++;
        }
        String message = unanswered == 0
                ? "Bạn có chắc chắn muốn nộp bài không?"
                : "Bạn còn " + unanswered + " câu chưa trả lời. Vẫn nộp bài?";
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài")
                .setMessage(message)
                .setPositiveButton("Nộp bài", (dialog, which) -> finishQuiz())
                .setNegativeButton("Tiếp tục làm", null)
                .show();
    }

    /*
     * Responsibility: Performs the `finish quiz` responsibility of `QuizPlayActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void finishQuiz() {
        if (quizSubmitted) return;
        if (countDownTimer != null) countDownTimer.cancel();

        score = 0;
        expEarned = 0;
        for (QuizQuestion question : questions) {
            if (question.getSelectedAnswer().equals(question.getCorrectAnswer())) {
                score++;
                expEarned += question.getExpReward();
            }
        }

        boolean saved = dbHelper.saveQuizAttemptAndAddExp(
                setId, score, questions.size(), expEarned);
        if (!saved) {
            Toast.makeText(this,
                    "Không thể lưu kết quả và EXP. Vui lòng thử lại.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        quizSubmitted = true;
        Toast.makeText(this, "+" + expEarned + " EXP Toán học", Toast.LENGTH_LONG).show();
        showResultDialog();
    }

    /*
     * Responsibility: Displays or binds data from `show result dialog` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showResultDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_result, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView txtScore = dialogView.findViewById(R.id.txt_score);
        TextView txtMessage = dialogView.findViewById(R.id.txt_result_message);
        TextView txtExp = dialogView.findViewById(R.id.txt_exp_earned);
        Button btnExplanations = dialogView.findViewById(R.id.btn_view_explanations);
        Button btnFinish = dialogView.findViewById(R.id.btn_finish_quiz);

        int totalExp = dbHelper.getUserExp();
        int level = MathLevelCalculator.getLevel(totalExp);
        txtScore.setText(score + "/" + questions.size());
        txtExp.setText("+" + expEarned + " EXP • Tổng " + totalExp
                + " EXP • Cấp " + level);

        if (score == questions.size()) {
            txtMessage.setText("Xuất sắc! Bạn đã trả lời đúng tất cả câu hỏi.");
        } else if (score * 2 >= questions.size()) {
            txtMessage.setText("Rất tốt! Hãy xem lời giải để củng cố các câu còn sai.");
        } else {
            txtMessage.setText("Hãy xem lời giải và thử lại với chủ đề phù hợp cấp học.");
        }

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        btnExplanations.setOnClickListener(v -> {
            dialog.dismiss();
            showExplanations();
        });
        dialog.show();
    }

    /*
     * Responsibility: Displays or binds data from `show explanations` to the appropriate UI or navigation destination.
     * Purpose: Separating presentation logic prevents data processing from being mixed with UI rendering.
     */
    private void showExplanations() {
        isExplanationMode = true;
        currentQuestionIndex = 0;
        displayQuestion();
    }

    /*
     * Responsibility: Handles the `handle exit` workflow or event and coordinates its related steps.
     * Purpose: A dedicated coordinator method makes the processing order clear and simplifies debugging and explanation.
     */
    private void handleExit() {
        if (quizSubmitted || isExplanationMode) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Thoát Quiz")
                .setMessage("Tiến độ chưa nộp sẽ không được lưu.")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Ở lại", null)
                .show();
    }

    /*
     * Responsibility: Handles the Android callback or event `onSaveInstanceState` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_INDEX, currentQuestionIndex);
        outState.putLong(STATE_TIME, timeLeftInMillis);
        outState.putBoolean(STATE_SUBMITTED, quizSubmitted);
        outState.putBoolean(STATE_EXPLANATION, isExplanationMode);
        outState.putInt(STATE_SCORE, score);
        outState.putInt(STATE_EXP, expEarned);
        ArrayList<String> answers = new ArrayList<>();
        for (QuizQuestion question : questions) answers.add(question.getSelectedAnswer());
        outState.putStringArrayList(STATE_ANSWERS, answers);
        super.onSaveInstanceState(outState);
    }

    /*
     * Responsibility: Performs the `restore state` responsibility of `QuizPlayActivity` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private void restoreState(Bundle state) {
        currentQuestionIndex = Math.max(0,
                Math.min(state.getInt(STATE_INDEX, 0), questions.size() - 1));
        timeLeftInMillis = Math.max(0L, state.getLong(STATE_TIME,
                Math.max(5, Math.min(30, questions.size())) * 60_000L));
        quizSubmitted = state.getBoolean(STATE_SUBMITTED, false);
        isExplanationMode = state.getBoolean(STATE_EXPLANATION, false);
        score = state.getInt(STATE_SCORE, 0);
        expEarned = state.getInt(STATE_EXP, 0);
        ArrayList<String> answers = state.getStringArrayList(STATE_ANSWERS);
        if (answers != null) {
            for (int i = 0; i < Math.min(answers.size(), questions.size()); i++) {
                questions.get(i).setSelectedAnswer(answers.get(i));
            }
        }
    }

    /*
     * Responsibility: Handles the Android callback or event `onDestroy` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
