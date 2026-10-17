package com.example.mentor.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.*;
import com.example.mentor.data.QuizCard;
import com.example.mentor.data.QuizPolicy;
import java.util.ArrayList;
import java.util.List;

/** One quiz experience; subject adapters only generate questions and save completed attempts. */
public final class QuizFormView extends LinearLayout {
  public interface Saver {
    void save(int correct, int count, int exp, Completion completion);
  }

  public interface Completion {
    void complete(boolean saved, int totalExp);
  }

  private final ArrayList<QuizQuestionCard> rows = new ArrayList<>();
  private final Button submit;
  private final TextView result;
  private Saver saver;
  private boolean saving, submitted;
  private int totalExp;
  private boolean timedOut, restoring;

  private void onMain(Runnable action) {
    if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) action.run();
    else post(action);
  }

  public QuizFormView(Context context) {
    this(context, null);
  }

  public QuizFormView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOrientation(VERTICAL);
    result = new TextView(context);
    result.setTextSize(17);
    result.setPadding(16, 24, 16, 24);
    result.setTextColor(getResources().getColor(R.color.mentor_quiz_text, context.getTheme()));
    result.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
    submit = new Button(context);
    submit.setMinHeight(Math.round(52 * getResources().getDisplayMetrics().density));
    submit.setAllCaps(false);
    submit.setText(R.string.mentor_quiz_submit);
    UiPalette.primary(submit);
    submit.setOnClickListener(v -> submit(false));
    setVisibility(GONE);
  }

  public void bind(List<QuizCard> cards, Saver saver) {
    removeAllViews();
    rows.clear();
    this.saver = saver;
    saving = false;
    submitted = false;
    timedOut = false;
    for (int i = 0; i < cards.size(); i++) {
      QuizQuestionCard row = new QuizQuestionCard(getContext(), cards.get(i), i + 1);
      rows.add(row);
      addView(row);
    }
    addView(submit, new LayoutParams(-1, -2));
    addView(result, new LayoutParams(-1, -2));
    submit.setEnabled(!rows.isEmpty());
    submit.setText(R.string.mentor_quiz_submit);
    result.setVisibility(GONE);
    setVisibility(rows.isEmpty() ? GONE : VISIBLE);
    if (!restoring && !rows.isEmpty())
      afterLayout(
          () -> {
            ScrollView scroll = scrollParent();
            if (scroll != null) scroll.smoothScrollTo(0, getTop());
          });
  }

  public void submit(boolean allowUnanswered) {
    if (saving || submitted || rows.isEmpty() || saver == null) return;
    for (QuizQuestionCard row : rows)
      if (!allowUnanswered && !timedOut && row.answer().trim().isEmpty()) {
        result.setText(R.string.mentor_quiz_answer_all);
        result.setVisibility(VISIBLE);
        row.requestFocus();
        row.requestRectangleOnScreen(
            new android.graphics.Rect(0, 0, row.getWidth(), row.getHeight()), false);
        return;
      }
    timedOut |= allowUnanswered;
    int correct = 0, exp = 0;
    for (QuizQuestionCard row : rows)
      if (row.correct()) {
        correct++;
        exp += QuizPolicy.reward(row.question.difficulty);
      }
    saving = true;
    submit.setEnabled(false);
    submit.setText(R.string.mentor_quiz_saving);
    for (QuizQuestionCard row : rows) row.setAnswersEnabled(false);
    final int score = correct, earned = exp;
    try {
      saver.save(
          correct,
          rows.size(),
          exp,
          (saved, total) ->
              onMain(
                  () -> {
                    if (!saving) return;
                    saving = false;
                    submitted = saved;
                    if (saved) {
                      totalExp = total;
                      showResult(score, earned);
                    } else {
                      for (QuizQuestionCard row : rows) row.setAnswersEnabled(!timedOut);
                      result.setText(R.string.mentor_quiz_save_error);
                      result.setVisibility(VISIBLE);
                      submit.setText(R.string.mentor_quiz_retry_save);
                      submit.setEnabled(true);
                    }
                  }));
    } catch (RuntimeException error) {
      saving = false;
      for (QuizQuestionCard row : rows) row.setAnswersEnabled(!timedOut);
      result.setText(R.string.mentor_quiz_save_error);
      result.setVisibility(VISIBLE);
      submit.setText(R.string.mentor_quiz_retry_save);
      submit.setEnabled(true);
    }
  }

  private void showResult(int score, int earned) {
    for (QuizQuestionCard row : rows) row.showFeedback();
    result.setText(
        getContext()
            .getString(
                R.string.mentor_quiz_result,
                score,
                rows.size(),
                Math.round(score * 100f / rows.size()),
                earned,
                totalExp,
                QuizPolicy.level(totalExp)));
    result.setVisibility(VISIBLE);
    submit.setEnabled(false);
    submit.setText(R.string.mentor_quiz_saved);
    afterLayout(
        () ->
            result.requestRectangleOnScreen(
                new android.graphics.Rect(0, 0, result.getWidth(), result.getHeight()), false));
  }

  public void confirmReplacement(Runnable action) {
    if (saving) return;
    if (rows.isEmpty() || submitted) {
      action.run();
      return;
    }
    new android.app.AlertDialog.Builder(getContext())
        .setTitle(R.string.mentor_quiz_exit)
        .setMessage(R.string.mentor_quiz_exit_message)
        .setPositiveButton(R.string.mentor_quiz_leave, (d, w) -> action.run())
        .setNegativeButton(R.string.mentor_quiz_continue, null)
        .show();
  }

  public boolean submitted() {
    return submitted;
  }

  public boolean saving() {
    return saving;
  }

  public ArrayList<String> answers() {
    ArrayList<String> result = new ArrayList<>();
    for (QuizQuestionCard row : rows) result.add(row.answer());
    return result;
  }

  private ScrollView scrollParent() {
    android.view.ViewParent parent = getParent();
    while (parent != null) {
      if (parent instanceof ScrollView) return (ScrollView) parent;
      parent = parent.getParent();
    }
    return null;
  }

  private void afterLayout(Runnable action) {
    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                action.run();
              }
            });
  }

  public Bundle state() {
    Bundle out = new Bundle();
    ArrayList<QuizCard> cards = new ArrayList<>();
    for (QuizQuestionCard row : rows) cards.add(row.question);
    ScrollView scroll = scrollParent();
    if (scroll != null) out.putInt("scroll", scroll.getScrollY());
    out.putSerializable("cards", cards);
    out.putStringArrayList("answers", answers());
    out.putBoolean("timedOut", timedOut);
    out.putBoolean("submitted", submitted);
    out.putInt("total", totalExp);
    return out;
  }

  @SuppressWarnings("unchecked")
  public boolean restore(Bundle state, Saver saver) {
    if (state == null) return false;
    Object cards = state.getSerializable("cards");
    if (!(cards instanceof ArrayList) || ((ArrayList<?>) cards).isEmpty()) return false;
    restoring = true;
    bind((ArrayList<QuizCard>) cards, saver);
    restoring = false;
    ArrayList<String> answers = state.getStringArrayList("answers");
    for (int i = 0; answers != null && i < Math.min(rows.size(), answers.size()); i++)
      rows.get(i).restore(answers.get(i));
    timedOut = state.getBoolean("timedOut");
    submitted = state.getBoolean("submitted");
    totalExp = state.getInt("total");
    if (timedOut && !submitted) for (QuizQuestionCard row : rows) row.setAnswersEnabled(false);
    if (submitted) {
      int correct = 0, exp = 0;
      for (QuizQuestionCard row : rows)
        if (row.correct()) {
          correct++;
          exp += QuizPolicy.reward(row.question.difficulty);
        }
      showResult(correct, exp);
    }
    afterLayout(
        () -> {
          ScrollView scroll = scrollParent();
          if (scroll != null) scroll.scrollTo(0, state.getInt("scroll", 0));
        });
    return true;
  }
}
