package com.brightpath.science.presentation.view;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.brightpath.science.R;
import com.brightpath.science.domain.model.QuizQuestion;

import java.util.List;
import java.util.Locale;

/** Accessible dynamic view for one adaptive Science quiz question. */
public final class QuizQuestionView extends LinearLayout {
    private final QuizQuestion question;
    private RadioGroup radioGroup;
    private EditText textAnswer;
    private final TextView feedbackView;

        /*
 * Responsibility: Initializes QuizQuestionView and receives or prepares the dependencies/state required by this custom Android View.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
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

        TextView difficultyView = new TextView(context);
        String diffLabel = context.getString(question.getDifficulty().getDisplayNameResId());
        difficultyView.setText(context.getString(R.string.science_quiz_difficulty_format, diffLabel, question.getExpReward()));
        difficultyView.setTextSize(14f);
        difficultyView.setTextColor(ContextCompat.getColor(context, R.color.science_quiz_color));
        difficultyView.setTypeface(difficultyView.getTypeface(), android.graphics.Typeface.BOLD);
        difficultyView.setPadding(0, dp(6), 0, 0);
        addView(difficultyView);

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

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `createRadioOptions`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private void createRadioOptions(Context context) {
        radioGroup = new RadioGroup(context);
        radioGroup.setPadding(0, dp(8), 0, 0);
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton rb = new RadioButton(context);
            rb.setText(options.get(i));
            rb.setTag(getOptionTag(i));
            rb.setTextColor(ContextCompat.getColor(context, R.color.science_text_secondary));
            rb.setTextSize(15f);
            rb.setPadding(dp(4), dp(8), dp(4), dp(8));
            radioGroup.addView(rb);
        }
        addView(radioGroup);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getOptionTag` from the appropriate custom Android View.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
private String getOptionTag(int index) {
        if (question.getType() == QuizQuestion.Type.TRUE_FALSE) {
            return index == 0 ? "TRUE" : "FALSE";
        }
        return String.valueOf((char) ('A' + index));
    }

        /*
 * Responsibility: Constructs the object, request, prompt, context, or data structure produced by `createTextAnswer`.
 * Purpose: Encapsulates creation rules so callers receive a consistent representation.
 */
private void createTextAnswer(Context context) {
        textAnswer = new EditText(context);
        textAnswer.setHint("Your answer");
        textAnswer.setInputType(InputType.TYPE_CLASS_TEXT);
        textAnswer.setTextColor(ContextCompat.getColor(context, R.color.science_text_primary));
        textAnswer.setHintTextColor(ContextCompat.getColor(context, R.color.science_text_secondary));
        textAnswer.setBackgroundResource(R.drawable.science_field_background);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        params.setMargins(0, dp(12), 0, 0);
        textAnswer.setLayoutParams(params);
        textAnswer.setPadding(dp(12), 0, dp(12), 0);
        addView(textAnswer);
    }

        /*
 * Responsibility: Restores the previously saved state handled by `restoreAnswer`.
 * Purpose: Allows the learner to continue the same Science flow after navigation, rotation, or reopening saved content.
 */
public void restoreAnswer(String answer) {
        if (answer == null || answer.isEmpty()) return;
        if (radioGroup != null) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
                if (answer.equals(rb.getTag())) {
                    rb.setChecked(true);
                    break;
                }
            }
        } else if (textAnswer != null) {
            textAnswer.setText(answer);
        }
    }

        /*
 * Responsibility: Checks the condition represented by `isCorrect` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean isCorrect() {
        String expected = normalize(question.getCorrectAnswer());
        String actual = normalize(getUserAnswer());
        return !actual.isEmpty() && expected.equals(actual);
    }

        /*
 * Responsibility: Checks the condition represented by `hasAnswer` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean hasAnswer() {
        return !normalize(getUserAnswer()).isEmpty();
    }

        /*
 * Responsibility: Retrieves the data/state required by `getUserAnswer` from the appropriate custom Android View.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getUserAnswer() {
        if (radioGroup != null) {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == -1) return "";
            RadioButton selected = radioGroup.findViewById(checkedId);
            return selected == null ? "" : String.valueOf(selected.getTag());
        }
        return textAnswer == null ? "" : textAnswer.getText().toString();
    }


        /*
 * Responsibility: Retrieves the data/state required by `getExpReward` from the appropriate custom Android View.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getExpReward() {
        return question.getExpReward();
    }

        /*
 * Responsibility: Prepares and presents the data/state handled by `showFeedback` on the UI.
 * Purpose: Transforms application state into information the learner can see and interact with.
 */
public void showFeedback() {
        boolean correct = isCorrect();
        feedbackView.setVisibility(VISIBLE);
        String feedback = correct
                ? getContext().getString(R.string.science_quiz_correct_feedback, getExpReward())
                : getContext().getString(R.string.science_quiz_wrong_feedback, question.getCorrectAnswer());
        feedbackView.setText(feedback + " " + question.getExplanation());
        feedbackView.setTextColor(ContextCompat.getColor(
                getContext(), correct ? R.color.science_success : R.color.science_error));
        feedbackView.announceForAccessibility(feedback);

        if (radioGroup != null) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(false);
            }
        }
        if (textAnswer != null) textAnswer.setEnabled(false);
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `normalize` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
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

        /*
 * Responsibility: Performs the `dp` operation for QuizQuestionView within the Science Module.
 * Purpose: Encapsulates this responsibility inside the custom Android View so the code is easier to read, test, maintain, and locate during the review.
 */
private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
