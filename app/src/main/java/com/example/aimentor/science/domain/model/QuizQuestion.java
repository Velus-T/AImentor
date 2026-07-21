package com.example.aimentor.science.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuizQuestion {
    public enum Type { MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK, MATCHING }

    private final Type type;
    private final String prompt;
    private final List<String> options;
    private final String correctAnswer;
    private final String explanation;

    public QuizQuestion(Type type, String prompt, List<String> options,
                        String correctAnswer, String explanation) {
        this.type = type == null ? Type.MULTIPLE_CHOICE : type;
        this.prompt = prompt == null ? "" : prompt;
        this.options = options == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(options));
        this.correctAnswer = correctAnswer == null ? "" : correctAnswer;
        this.explanation = explanation == null ? "" : explanation;
    }

    public Type getType() { return type; }
    public String getPrompt() { return prompt; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
}
