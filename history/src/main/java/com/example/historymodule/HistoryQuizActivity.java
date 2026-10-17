package com.example.historymodule;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.historymodule.api.HistoryAiService;
import com.example.historymodule.databases.SqliteDbHelper;
import com.example.historymodule.model.HistoryQuizQuestion;
import com.example.historymodule.util.HistoryUserSession;
import com.example.historymodule.util.QuizCards;
import com.example.mentor.ui.QuizFormView;
import com.example.mentor.ui.QuizSetupView;
import java.util.List;

/** History adapter for the shared quiz UI and offline bank. */
public final class HistoryQuizActivity extends AppCompatActivity {
  public static final String EXTRA_SCOPE = "history_quiz_scope";
  public static final String EXTRA_TOPIC = "history_quiz_topic";
  public static final String EXTRA_EDUCATION_LEVEL = "history_quiz_education_level";
  private static final String[] SCOPES = {"Lịch sử Việt Nam", "Lịch sử nước ngoài", "Tổng hợp"};
  private static final String[] LEVELS = {"Trung học cơ sở", "Trung học phổ thông", "Đại học"};
  private QuizSetupView setup;
  private QuizFormView form;
  private HistoryAiService service;
  private SqliteDbHelper database;
  private String activeTopic, activeLevel;
  private int userId;
  private boolean destroyed;

  @Override
  protected void onCreate(@Nullable Bundle state) {
    super.onCreate(state);
    setContentView(com.example.mentor.ui.R.layout.mentor_quiz_page);
    userId = HistoryUserSession.getUserId(this);
    service = new HistoryAiService(this);
    database = new SqliteDbHelper(this);
    setup = findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup);
    form = findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
    setup.configure(
        new String[] {
          getString(R.string.history_quiz_topic_vn),
          getString(R.string.history_quiz_topic_foreign),
          getString(R.string.history_quiz_topic_mixed)
        },
        new String[] {
          getString(R.string.history_quiz_edu_ms),
          getString(R.string.history_quiz_edu_hs),
          getString(R.string.history_quiz_edu_uni)
        },
        com.example.mentor.data.QuizPolicy.questionCounts());
    String requested = getIntent().getStringExtra(EXTRA_SCOPE);
    for (int i = 0; i < SCOPES.length; i++)
      if (SCOPES[i].equals(requested)) setup.scope.setSelection(i);
    String requestedLevel = getIntent().getStringExtra(EXTRA_EDUCATION_LEVEL);
    for (int i = 0; i < LEVELS.length; i++)
      if (LEVELS[i].equals(requestedLevel)) setup.level.setSelection(i);
    setup.create.setOnClickListener(v -> form.confirmReplacement(this::generate));
    setup.attachBank(
        userId,
        "history",
        (set, cards) ->
            form.confirmReplacement(
                () -> {
                  activeTopic = set.topic;
                  activeLevel = set.level;
                  form.bind(cards, this::save);
                  setup.notice(com.example.mentor.ui.R.string.mentor_quiz_bank_source);
                }));
    if (state != null) {
      setup.restore(state.getBundle("setup"));
      activeTopic = state.getString("topic");
      activeLevel = state.getString("level");
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
                form.confirmReplacement(HistoryQuizActivity.this::finish);
              }
            });
  }

  private void generate() {
    String scope = SCOPES[setup.scope.getSelectedItemPosition()];
    String topic = setup.topic().isEmpty() ? scope : setup.topic();
    String level = LEVELS[setup.level.getSelectedItemPosition()];
    setup.loading(true);
    service.generateQuiz(
        scope,
        topic,
        level,
        setup.count(),
        new HistoryAiService.Callback<List<HistoryQuizQuestion>>() {
          @Override
          public void onSuccess(List<HistoryQuizQuestion> questions) {
            if (destroyed) return;
            setup.loading(false);
            activeTopic = topic;
            activeLevel = level;
            form.bind(QuizCards.from(questions), HistoryQuizActivity.this::save);
            setup.notice(com.example.mentor.ui.R.string.mentor_quiz_new_ai);
          }

          @Override
          public void onError(String message, Throwable error) {
            if (!destroyed) setup.error();
          }
        });
  }

  private void save(int correct, int count, int exp, QuizFormView.Completion completion) {
    long id = database.saveQuizAttempt(userId, activeTopic, activeLevel, correct, count, exp);
    completion.complete(id > 0, database.getTotalQuizExp(userId));
  }

  @Override
  protected void onSaveInstanceState(Bundle out) {
    super.onSaveInstanceState(out);
    out.putBundle("setup", setup.state());
    out.putBundle("form", form.state());
    out.putString("topic", activeTopic);
    out.putString("level", activeLevel);
  }

  @Override
  protected void onDestroy() {
    destroyed = true;
    service.shutdown();
    database.close();
    super.onDestroy();
  }
}
