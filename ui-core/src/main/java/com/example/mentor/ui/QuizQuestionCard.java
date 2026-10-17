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
        int difficulty = "EASY".equals(question.difficulty) ? R.string.mentor_quiz_easy
                : "HARD".equals(question.difficulty) ? R.string.mentor_quiz_hard : R.string.mentor_quiz_medium;
        addView(label(context.getString(R.string.mentor_quiz_reward, context.getString(difficulty),
                QuizPolicy.reward(question.difficulty)), 13));
        if (!question.options.isEmpty()) {
            choices = new RadioGroup(context);
            for (int i = 0; i < question.options.size(); i++) {
                RadioButton choice = new RadioButton(context);
                choice.setId(View.generateViewId());
                choice.setTag(question.optionValues.get(i));
                choice.setText(question.math ? question.optionValues.get(i) : question.options.get(i));
                choice.setTextSize(16);
                choice.setTextColor(color(R.color.mentor_quiz_text));
                choice.setMinHeight(dp(52));
                choice.setPadding(dp(4), dp(10), dp(4), dp(10));
                choices.addView(choice, new RadioGroup.LayoutParams(-1, -2));
                if (question.math) {
                    View formula = content(question.options.get(i), true, 16);
                    formula.setOnClickListener(v -> choice.setChecked(true));
                    choices.addView(formula);
                }
            }
            addView(choices);
        } else {
            written = new EditText(context);
            written.setHint(R.string.mentor_quiz_answer);
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
        explanation=content(question.explanation,question.math,15); explanation.setVisibility(GONE); addView(explanation);
    }
    private int color(int id) { return getResources().getColor(id, getContext().getTheme()); }
    private TextView label(String text, int size) {
        TextView view = new TextView(getContext());
        view.setText(text); view.setTextSize(size); view.setTextColor(color(R.color.mentor_quiz_text));
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }
    private View content(String text, boolean math, int size) {
        if (!math) return label(text, size);
        WebView web = new WebView(getContext());
        web.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        String escaped = android.text.TextUtils.htmlEncode(text).replace("\n", "<br>");
        String color = String.format(java.util.Locale.ROOT, "#%06X", 0xFFFFFF & color(R.color.mentor_quiz_text));
        web.loadDataWithBaseURL("https://cdn.jsdelivr.net/", "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css'>"
                + "<script defer src='https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js'></script>"
                + "<script defer src='https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js' "
                + "onload=\"renderMathInElement(document.body,{delimiters:[{left:'$$',right:'$$',display:true},{left:'$',right:'$',display:false}],throwOnError:false})\"></script>"
                + "<style>body{margin:0;padding:8px 0;color:" + color + ";font:" + size
                + "px sans-serif;overflow-wrap:anywhere}.katex-display{overflow-x:auto}</style>" + escaped,
                "text/html", "UTF-8", null);
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
        else for (int i = 0; i < choices.getChildCount(); i++) {
            View view = choices.getChildAt(i);
            if (view instanceof RadioButton) ((RadioButton) view).setChecked(value.equals(view.getTag()));
        }
    }
    public boolean correct() { return question.correct(answer()); }
    public void showFeedback() {
        String line = correct() ? getContext().getString(R.string.mentor_quiz_correct, QuizPolicy.reward(question.difficulty))
                : getContext().getString(R.string.mentor_quiz_wrong, question.answer);
        feedback.setText(line);
        explanation.setVisibility(VISIBLE);
        feedback.setVisibility(VISIBLE);
        setAnswersEnabled(false);
    }
    public void setAnswersEnabled(boolean enabled) {
        if (written != null) written.setEnabled(enabled);
        if (choices != null) for (int i = 0; i < choices.getChildCount(); i++) choices.getChildAt(i).setEnabled(enabled);
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
