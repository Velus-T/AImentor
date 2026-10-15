package com.example.aimentor.science.presentation;

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
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.R;
import com.example.aimentor.science.di.ScienceModule;
import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.GeneratedQuiz;
import com.example.aimentor.science.domain.model.QuizQuestion;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.repository.QuizAttemptRepository;
import com.example.aimentor.science.domain.service.ResultCallback;
import com.example.aimentor.science.domain.usecase.GenerateScienceQuizUseCase;
import com.example.aimentor.science.presentation.view.QuizQuestionView;
import com.example.aimentor.science.util.SciencePreferenceStore;
import com.example.aimentor.science.util.ScienceUserSession;
import com.example.aimentor.science.util.ScienceUiFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ScienceQuizActivity extends com.example.aimentor.ui.InsetActivity {
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

    private GenerateScienceQuizUseCase generateQuizUseCase;
    private QuizAttemptRepository attemptRepository;
    private int userId;
    private QuizRequest activeQuizRequest;
    private List<QuizQuestion> activeQuestions = new ArrayList<>();
    private final List<QuizQuestionView> questionViews = new ArrayList<>();
    private boolean loading;
    private volatile boolean destroyed;

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
    }

    private void setupSpinners() {
        subjectSpinner.setAdapter(adapter(new String[]{"Physics", "Chemistry", "Biology"}));

        String[] levels = new String[EducationLevel.values().length];
        for (int i = 0; i < levels.length; i++) levels[i] = EducationLevel.values()[i].getDisplayName();
        levelSpinner.setAdapter(adapter(levels));
        countSpinner.setAdapter(adapter(new String[]{"3 questions", "4 questions", "5 questions"}));

        SciencePreferenceStore preferences = new SciencePreferenceStore(this);
        subjectSpinner.setSelection(preferences.getSubject().ordinal());
        levelSpinner.setSelection(preferences.getLevelPosition());
        countSpinner.setSelection(2);
    }

    private android.widget.ArrayAdapter<String> adapter(String[] values) {
        return ScienceUiFactory.spinnerAdapter(this, values);
    }


    private void restoreQuizInputs(QuizRequest request) {
        if (request == null) return;
        subjectSpinner.setSelection(request.getSubject().ordinal());
        levelSpinner.setSelection(request.getLevel().ordinal());
        countSpinner.setSelection(Math.max(0, Math.min(2, request.getNumberOfQuestions() - 3)));
        topicInput.setText(request.getTopic());
    }

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

    private void submitQuiz() {
        if (activeQuizRequest == null || questionViews.isEmpty()) return;
        for (int i = 0; i < questionViews.size(); i++) {
            if (!questionViews.get(i).hasAnswer()) {
                Toast.makeText(this, "Answer question " + (i + 1) + " before submitting.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int correct = 0;
        for (QuizQuestionView questionView : questionViews) {
            if (questionView.isCorrect()) correct++;
            questionView.showFeedback();
        }

        try {
            attemptRepository.saveAttempt(
                    userId,
                    activeQuizRequest.getSubject(),
                    activeQuizRequest.getLevel(),
                    activeQuizRequest.getTopic(),
                    activeQuizRequest.getChallenge(),
                    questionViews.size(),
                    correct
            );
        } catch (Exception error) {
            Toast.makeText(this,
                    "Score shown, but progress could not be saved.",
                    Toast.LENGTH_SHORT).show();
        }

        double percentage = correct * 100.0 / questionViews.size();
        String nextMode;
        if (percentage < 50) {
            nextMode = "The next quiz at this same education level will provide more support.";
        } else if (percentage >= 80) {
            nextMode = "The next quiz at this same education level will be more challenging.";
        } else {
            nextMode = "The next quiz will stay on-level but use different questions.";
        }

        resultView.setText(String.format(Locale.getDefault(),
                "Score: %d/%d (%.0f%%)\n%s",
                correct, questionViews.size(), percentage, nextMode));
        resultView.setVisibility(View.VISIBLE);
        resultView.announceForAccessibility(resultView.getText());
        submitButton.setEnabled(false);
    }

    private ScienceSubject selectedSubject() {
        int position = subjectSpinner.getSelectedItemPosition();
        if (position == 1) return ScienceSubject.CHEMISTRY;
        if (position == 2) return ScienceSubject.BIOLOGY;
        return ScienceSubject.PHYSICS;
    }

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
