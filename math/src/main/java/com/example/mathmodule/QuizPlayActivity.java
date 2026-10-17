package com.example.mathmodule;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mathmodule.databases.*;
import com.example.mathmodule.util.QuizCards;
import com.example.mentor.ui.QuizFormView;
import java.util.*;

/** Mathematics uses the common quiz form, retaining formula rendering without a time limit. */
public final class QuizPlayActivity extends AppCompatActivity {
  private QuizFormView form;
  private SqliteDbHelper database;
  private int setId, userId;

  @Override
  protected void onCreate(@Nullable Bundle state) {
    super.onCreate(state);
    setContentView(com.example.mentor.ui.R.layout.mentor_quiz_page);
    database = new SqliteDbHelper(this);
    userId = com.example.mentor.data.ActiveUser.get(this);
    setId = getIntent().getIntExtra("QUIZ_SET_ID", -1);
    List<QuizQuestion> questions = database.getQuestionsBySetId(setId);
    if (questions.isEmpty()) {
      finish();
      return;
    }
    findViewById(com.example.mentor.ui.R.id.mentor_quiz_setup).setVisibility(View.GONE);
    TextView title = findViewById(com.example.mentor.ui.R.id.mentor_quiz_heading);
    String heading = getIntent().getStringExtra("QUIZ_SET_TITLE");
    if (heading != null && !heading.trim().isEmpty()) title.setText(heading);
    findViewById(com.example.mentor.ui.R.id.mentor_quiz_status).setVisibility(View.GONE);
    form = findViewById(com.example.mentor.ui.R.id.mentor_quiz_form);
    if (state == null || !form.restore(state.getBundle("form"), this::save))
      form.bind(QuizCards.from(questions), this::save);
    findViewById(com.example.mentor.ui.R.id.mentor_quiz_back)
        .setOnClickListener(v -> form.confirmReplacement(this::finish));
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                form.confirmReplacement(QuizPlayActivity.this::finish);
              }
            });
  }

  private void save(int correct, int count, int exp, QuizFormView.Completion completion) {
    // The owner snapshot must still match before using the legacy subject helper.
    if (com.example.mentor.data.ActiveUser.get(this) != userId) {
      completion.complete(false, 0);
      return;
    }
    boolean saved = database.saveQuizAttemptAndAddExp(setId, correct, count, exp);
    completion.complete(saved, database.getUserExp());
  }

  @Override
  protected void onSaveInstanceState(Bundle out) {
    super.onSaveInstanceState(out);
    if (form != null) out.putBundle("form", form.state());
  }

  @Override
  protected void onDestroy() {
    if (database != null) database.close();
    super.onDestroy();
  }
}
