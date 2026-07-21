package com.example.aimentor.science.presentation.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.aimentor.R;
import com.example.aimentor.science.domain.model.QuizQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Accessible dynamic view for one adaptive Science quiz question. */
public final class QuizQuestionView extends LinearLayout {
    private final QuizQuestion question;
    private RadioGroup radioGroup;
    private EditText textAnswer;
    private final TextView feedbackView;

    public QuizQuestionView(Context context, QuizQuestion question, int number) {
        super(context);
        this.question = question;
        setOrientation(VERTICAL);
        setPadding(dp(14), dp(14), dp(14), dp(14));
        setBackgroundResource(R.drawable.science_panel);
        LayoutParams cardParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(10));
        setLayoutParams(cardParams);

        TextView prompt = new TextView(context);
        prompt.setText(number + ". " + question.getPrompt());
        prompt.setTextSize(16f);
        prompt.setTextColor(ContextCompat.getColor(context, R.color.science_text_primary));
        prompt.setTypeface(prompt.getTypeface(), android.graphics.Typeface.BOLD);
        prompt.setLineSpacing(0f, 1.12f);
        addView(prompt);

        if (question.getType() == QuizQuestion.Type.MULTIPLE_CHOICE
                || question.getType() == QuizQuestion.Type.TRUE_FALSE) {
            createRadioOptions(context);
        } else {
            createTextAnswer(context);
        }

        feedbackView = new TextView(context);
        feedbackView.setPadding(0, dp(10), 0, 0);
        feedbackView.setTextSize(14f);
        feedbackView.setLineSpacing(0f, 1.12f);
        feedbackView.setVisibility(GONE);
        feedbackView.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        addView(feedbackView);
    }

    private void createRadioOptions(Context context) {
        radioGroup = new RadioGroup(context);
        radioGroup.setOrientation(VERTICAL);
        List<String> options = new ArrayList<>(question.getOptions());
        if (options.isEmpty() && question.getType() == QuizQuestion.Type.TRUE_FALSE) {
            options.add("True");
            options.add("False");
        }
        ColorStateList tint = ContextCompat.getColorStateList(context, R.color.science_control_tint);
        ColorStateList text = ContextCompat.getColorStateList(context, R.color.science_option_text);
        for (String option : options) {
            RadioButton button = new RadioButton(context);
            button.setId(View.generateViewId());
            button.setText(option);
            button.setTag(option);
            button.setMinHeight(dp(48));
            button.setButtonTintList(tint);
            button.setTextColor(text);
            button.setTextSize(15f);
            button.setBackgroundResource(R.drawable.science_option_background);
            button.setPadding(dp(10), dp(4), dp(10), dp(4));
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, dp(8), 0, 0);
            radioGroup.addView(button, params);
        }
        addView(radioGroup);
    }

    private void createTextAnswer(Context context) {
        if (question.getType() == QuizQuestion.Type.MATCHING) {
            TextView options = new TextView(context);
            options.setText(joinOptions());
            options.setTextColor(ContextCompat.getColor(context, R.color.science_text_secondary));
            options.setTextSize(14f);
            options.setPadding(0, dp(8), 0, dp(4));
            addView(options);
        }
        textAnswer = new EditText(context);
        textAnswer.setHint(question.getType() == QuizQuestion.Type.MATCHING
                ? "Enter mapping, e.g. A-2, B-1"
                : "Enter your answer");
        textAnswer.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        textAnswer.setMinHeight(dp(52));
        textAnswer.setTextColor(ContextCompat.getColor(context, R.color.science_text_primary));
        textAnswer.setHintTextColor(ContextCompat.getColor(context, R.color.science_text_secondary));
        textAnswer.setBackgroundResource(R.drawable.science_field_background);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, 0);
        addView(textAnswer, params);
    }


    /** Restores an answer after configuration changes without changing quiz content. */
    public void restoreAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) return;
        if (radioGroup != null) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                View child = radioGroup.getChildAt(i);
                if (child instanceof RadioButton
                        && answer.equals(String.valueOf(child.getTag()))) {
                    ((RadioButton) child).setChecked(true);
                    return;
                }
            }
        } else if (textAnswer != null) {
            textAnswer.setText(answer);
            textAnswer.setSelection(textAnswer.length());
        }
    }

    public boolean isCorrect() {
        String expected = normalize(question.getCorrectAnswer());
        String actual = normalize(getUserAnswer());
        return !actual.isEmpty() && expected.equals(actual);
    }

    public boolean hasAnswer() {
        return !normalize(getUserAnswer()).isEmpty();
    }

    public String getUserAnswer() {
        if (radioGroup != null) {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == -1) return "";
            RadioButton selected = radioGroup.findViewById(checkedId);
            return selected == null ? "" : String.valueOf(selected.getTag());
        }
        return textAnswer == null ? "" : textAnswer.getText().toString();
    }

    public void showFeedback() {
        boolean correct = isCorrect();
        feedbackView.setVisibility(VISIBLE);
        feedbackView.setText((correct ? "Correct. "
                : "Correct answer: " + question.getCorrectAnswer() + ". ")
                + question.getExplanation());
        feedbackView.setTextColor(ContextCompat.getColor(
                getContext(), correct ? R.color.science_success : R.color.science_error));
        feedbackView.announceForAccessibility(feedbackView.getText());
        if (radioGroup != null) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(false);
            }
        }
        if (textAnswer != null) textAnswer.setEnabled(false);
    }

    private String joinOptions() {
        StringBuilder builder = new StringBuilder();
        for (String option : question.getOptions()) builder.append(option).append('\n');
        return builder.toString().trim();
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT)
                .replace('₀', '0').replace('₁', '1').replace('₂', '2')
                .replace('₃', '3').replace('₄', '4').replace('₅', '5')
                .replace('₆', '6').replace('₇', '7').replace('₈', '8').replace('₉', '9')
                .replace('⁰', '0').replace('¹', '1').replace('²', '2').replace('³', '3')
                .replace('⁴', '4').replace('⁵', '5').replace('⁶', '6')
                .replace('⁷', '7').replace('⁸', '8').replace('⁹', '9')
                .replaceAll("[^\\p{L}0-9]", "");
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
