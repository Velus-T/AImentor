package com.brightpath.science.presentation;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.brightpath.science.di.ScienceModule;
import com.brightpath.science.domain.model.*;
import com.brightpath.science.domain.repository.QuizAttemptRepository;
import com.brightpath.science.domain.service.ResultCallback;
import com.brightpath.science.util.*;
import com.example.mentor.ui.QuizFormView;
import com.example.mentor.ui.QuizSetupView;

/** Science source/storage adapter for the common quiz experience. */
public final class ScienceQuizActivity extends AppCompatActivity {
  private QuizSetupView setup;
  private QuizFormView form;
  private QuizAttemptRepository attempts;
  private QuizRequest activeRequest;
  private int userId;
  private boolean destroyed;

  @Override
  protected void onCreate(@Nullable Bundle state) {
    super.onCreate(state);
    setContentView(com.example.mentor.ui.R.layout.mentor_quiz_page);
    userId = new ScienceUserSession(this).getUserId();
    attempts = ScienceModule.get(this).quizAttemptRepository();
    setup = findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup);
    form = findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
    String[] scopes = new String[ScienceSubject.values().length];
    for (int i = 0; i < scopes.length; i++)
      scopes[i] = getString(ScienceSubject.values()[i].getDisplayNameResId());
    String[] levels = new String[EducationLevel.values().length];
    for (int i = 0; i < levels.length; i++)
      levels[i] = getString(EducationLevel.values()[i].getDisplayNameResId());
    setup.configure(scopes, levels, com.example.mentor.data.QuizPolicy.questionCounts());
    SciencePreferenceStore preferences = new SciencePreferenceStore(this);
    setup.scope.setSelection(preferences.getSubject().ordinal());
    setup.level.setSelection(preferences.getLevelPosition());
    setup.create.setOnClickListener(v -> form.confirmReplacement(this::generate));
    setup.attachBank(
        userId,
        "science",
        (set, cards) ->
            form.confirmReplacement(
                () -> {
                  activeRequest =
                      new QuizRequest(
                          ScienceSubject.fromCode(set.scope),
                          EducationLevel.valueOf(set.level),
                          set.topic,
                          cards.size());
                  form.bind(cards, this::save);
                  setup.notice(com.example.mentor.ui.R.string.mentor_quiz_bank_source);
                }));
    if (state != null) {
      setup.restore(state.getBundle("setup"));
      if (state.containsKey("activeSubject"))
        activeRequest =
            new QuizRequest(
                    ScienceSubject.fromCode(state.getString("activeSubject")),
                    EducationLevel.valueOf(state.getString("activeLevel")),
                    state.getString("activeTopic"),
                    state.getInt("activeCount"))
                .withAdaptiveContext(
                    userId,
                    QuizChallenge.valueOf(state.getString("challenge")),
                    QuizPerformance.empty(),
                    0,
                    java.util.Collections.emptyList());
      form.restore(state.getBundle("form"), this::save);
    }
    findViewById(com.example.mentor.ui.R.id.mentor_quiz_back)
        .setOnClickListener(v -> form.confirmReplacement(this::finish));
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                form.confirmReplacement(ScienceQuizActivity.this::finish);
              }
            });
  }

  private void generate() {
    QuizRequest request =
        new QuizRequest(
            ScienceSubject.values()[setup.scope.getSelectedItemPosition()],
            EducationLevel.fromPosition(setup.level.getSelectedItemPosition()),
            setup.topic(),
            setup.count());
    setup.loading(true);
    ScienceModule.get(this)
        .generateScienceQuizUseCase()
        .execute(
            userId,
            request,
            new ResultCallback<GeneratedQuiz>() {
              @Override
              public void onSuccess(GeneratedQuiz quiz) {
                if (destroyed) return;
                setup.loading(false);
                activeRequest = quiz.getRequest();
                if (quiz.getQuestions().stream().allMatch(QuizQuestion::isLocal)) {
                  QuizRequest original = activeRequest;
                  activeRequest =
                      new QuizRequest(
                              original.getSubject(),
                              original.getLevel(),
                              "General science",
                              original.getNumberOfQuestions())
                          .withAdaptiveContext(
                              userId,
                              original.getChallenge(),
                              original.getRecentPerformance(),
                              original.getVariationSeed(),
                              original.getRecentlyUsedPrompts());
                }
                form.bind(QuizCards.from(quiz.getQuestions()), ScienceQuizActivity.this::save);
                setup.notice(
                    quiz.getQuestions().stream().allMatch(QuizQuestion::isLocal)
                        ? com.example.mentor.ui.R.string.mentor_quiz_local_source
                        : com.example.mentor.ui.R.string.mentor_quiz_new_ai);
              }

              @Override
              public void onError(String message, Throwable error) {
                if (destroyed) return;
                setup.error();
                if (error instanceof InsufficientLocalQuizException) {
                  InsufficientLocalQuizException shortage = (InsufficientLocalQuizException) error;
                  setup.notice(
                      com.example.mentor.ui.R.string.mentor_quiz_local_shortage,
                      shortage.available,
                      shortage.requested);
                }
              }
            });
  }

  private void save(int correct, int count, int exp, QuizFormView.Completion completion) {
    if (activeRequest == null) {
      completion.complete(false, attempts.getTotalExp(userId));
      return;
    }
    long id =
        attempts.saveAttempt(
            userId,
            activeRequest.getSubject(),
            activeRequest.getLevel(),
            activeRequest.getTopic(),
            activeRequest.getChallenge(),
            count,
            correct,
            exp);
    completion.complete(id > 0, attempts.getTotalExp(userId));
  }

  @Override
  protected void onSaveInstanceState(Bundle out) {
    super.onSaveInstanceState(out);
    out.putBundle("setup", setup.state());
    out.putBundle("form", form.state());
    if (activeRequest != null) {
      out.putString("activeSubject", activeRequest.getSubject().getCode());
      out.putString("activeLevel", activeRequest.getLevel().name());
      out.putString("activeTopic", activeRequest.getTopic());
      out.putInt("activeCount", activeRequest.getNumberOfQuestions());
      out.putString("challenge", activeRequest.getChallenge().name());
    }
  }

  @Override
  protected void onDestroy() {
    destroyed = true;
    super.onDestroy();
  }
}
