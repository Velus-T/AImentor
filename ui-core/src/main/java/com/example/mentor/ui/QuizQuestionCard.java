package com.example.mentor.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import com.example.mentor.data.QuizCard;
import com.example.mentor.data.QuizPolicy;

/** Shared answer controls and feedback for every subject, including formulas and text answers. */
public final class QuizQuestionCard extends LinearLayout {
  public final QuizCard question;
  private RadioGroup choices;
  private final java.util.List<RadioButton> optionButtons = new java.util.ArrayList<>();
  private EditText written;
  private final TextView feedback;
  private final View explanation;

  public QuizQuestionCard(Context context, QuizCard question, int number) {
    super(context);
    this.question = question;
    setOrientation(VERTICAL);
    setPadding(dp(16), dp(16), dp(16), dp(16));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(getResources().getColor(R.color.mentor_quiz_surface, context.getTheme()));
    bg.setCornerRadius(dp(16));
    setBackground(bg);
    LayoutParams params = new LayoutParams(-1, -2);
    params.setMargins(0, dp(10), 0, dp(6));
    setLayoutParams(params);
    TextView heading = label(context.getString(R.string.mentor_quiz_question, number), 13);
    heading.setTypeface(null, Typeface.BOLD);
    addView(heading);
    addView(content(question.prompt, question.math, 18));
    int difficulty =
        "EASY".equals(question.difficulty)
            ? R.string.mentor_quiz_easy
            : "HARD".equals(question.difficulty)
                ? R.string.mentor_quiz_hard
                : R.string.mentor_quiz_medium;
    addView(
        label(
            context.getString(
                R.string.mentor_quiz_reward,
                context.getString(difficulty),
                QuizPolicy.reward(question.difficulty)),
            13));
    if (!question.options.isEmpty()) {
      choices = new RadioGroup(context);
      for (int i = 0; i < question.options.size(); i++) {
        RadioButton choice = new RadioButton(context);
        choice.setId(View.generateViewId());
        choice.setTag(question.optionValues.get(i));
        choice.setText(question.math ? question.optionValues.get(i) : question.options.get(i));
        choice.setTextSize(16);
        choice.setButtonTintList(
            getResources().getColorStateList(R.color.mentor_quiz_choice_tint, context.getTheme()));
        choice.setTextColor(color(R.color.mentor_quiz_text));
        choice.setMinHeight(dp(52));
        choice.setPadding(dp(4), dp(10), dp(4), dp(10));
        optionButtons.add(choice);
        choice.setOnClickListener(v -> choices.check(choice.getId()));
        if (question.math) {
          LinearLayout option = new LinearLayout(context);
          option.setOrientation(HORIZONTAL);
          option.setGravity(android.view.Gravity.CENTER_VERTICAL);
          option.addView(choice, new LayoutParams(dp(72), -2));
          View formula = content(question.options.get(i), true, 16);
          formula.setOnClickListener(
              v -> {
                if (choice.isEnabled()) choices.check(choice.getId());
              });
          android.view.GestureDetector taps =
              new android.view.GestureDetector(
                  context,
                  new android.view.GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(android.view.MotionEvent event) {
                      return true;
                    }

                    @Override
                    public boolean onSingleTapUp(android.view.MotionEvent event) {
                      formula.performClick();
                      return true;
                    }
                  });
          formula.setOnTouchListener(
              (v, event) -> {
                taps.onTouchEvent(event);
                return false;
              });
          option.addView(formula, new LayoutParams(0, -2, 1));
          choices.addView(option, new RadioGroup.LayoutParams(-1, -2));
        } else choices.addView(choice, new RadioGroup.LayoutParams(-1, -2));
      }
      addView(choices);
    } else {
      written = new EditText(context);
      written.setHint(
          "MATCHING".equals(question.answerType())
              ? R.string.mentor_quiz_matching_hint
              : R.string.mentor_quiz_answer);
      written.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
      written.setMinHeight(dp(52));
      written.setTextColor(color(R.color.mentor_quiz_text));
      written.setHintTextColor(color(R.color.mentor_quiz_secondary));
      addView(written, new LayoutParams(-1, -2));
    }
    feedback = label("", 15);
    feedback.setVisibility(GONE);
    feedback.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
    addView(feedback);
    explanation = content(question.explanation, question.math, 15);
    explanation.setVisibility(GONE);
    addView(explanation);
  }

  private int color(int id) {
    return getResources().getColor(id, getContext().getTheme());
  }

  private TextView label(String text, int size) {
    TextView view = new TextView(getContext());
    view.setText(text);
    view.setTextSize(size);
    view.setTextColor(color(R.color.mentor_quiz_text));
    view.setPadding(0, dp(6), 0, dp(6));
    return view;
  }

  private View content(String text, boolean math, int size) {
    if (!math) return label(text, size);
    WebView web = new WebView(getContext());
    MathTextRenderer.render(web, text, size);
    web.setLayoutParams(new LayoutParams(-1, -2));
    return web;
  }

  public String answer() {
    if (written != null) return written.getText().toString();
    RadioButton selected = choices.findViewById(choices.getCheckedRadioButtonId());
    return selected == null ? "" : String.valueOf(selected.getTag());
  }

  public void restore(String value) {
    if (written != null) written.setText(value);
    else {
      choices.clearCheck();
      for (RadioButton option : optionButtons)
        if (value.equals(option.getTag())) choices.check(option.getId());
    }
  }

  public boolean correct() {
    return question.correct(answer());
  }

  public void showFeedback() {
    String line =
        correct()
            ? getContext()
                .getString(R.string.mentor_quiz_correct, QuizPolicy.reward(question.difficulty))
            : getContext().getString(R.string.mentor_quiz_wrong, question.answer);
    feedback.setText(line);
    feedback.setTextColor(
        color(correct() ? R.color.mentor_quiz_success : R.color.mentor_quiz_error));
    explanation.setVisibility(VISIBLE);
    feedback.setVisibility(VISIBLE);
    setAnswersEnabled(false);
  }

  public void setAnswersEnabled(boolean enabled) {
    if (written != null) written.setEnabled(enabled);
    for (RadioButton option : optionButtons) option.setEnabled(enabled);
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }
}
