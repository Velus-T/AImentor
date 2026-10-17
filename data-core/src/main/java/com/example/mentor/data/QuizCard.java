package com.example.mentor.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Presentation-independent question; adapters retain each subject's existing source and storage. */
public final class QuizCard implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String prompt, answer, explanation, difficulty;
    public final ArrayList<String> options;
    public final ArrayList<String> optionValues;
    public final boolean math;

    public QuizCard(String prompt, List<String> options, List<String> values, String answer,
                    String explanation, String difficulty, boolean math) {
        this.prompt = prompt == null ? "" : prompt;
        this.options = new ArrayList<>(options);
        this.optionValues = new ArrayList<>(values);
        if (this.options.size() != this.optionValues.size()) throw new IllegalArgumentException("Option values");
        this.answer = answer == null ? "" : answer;
        this.explanation = explanation == null ? "" : explanation;
        this.difficulty = difficulty == null ? "MEDIUM" : difficulty;
        this.math = math;
    }
    public boolean correct(String actual) {
        return actual != null && !actual.trim().isEmpty() && normalize(answer).equals(normalize(actual));
    }
    private static String normalize(String value) {
        // Keep signs and punctuation: -2 must never equal 2, and 1.5 must never equal 15.
        String raw = value.trim().toLowerCase(Locale.ROOT);
        String subs = "₀₁₂₃₄₅₆₇₈₉", supers = "⁰¹²³⁴⁵⁶⁷⁸⁹";
        for (int i = 0; i < 10; i++) raw = raw.replace(subs.charAt(i), (char) ('0' + i))
                .replace(supers.charAt(i), (char) ('0' + i));
        return raw.replaceAll("\\s+", " ");
    }
    public static ArrayList<String> letters(int count) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) result.add(String.valueOf((char) ('A' + i)));
        return result;
    }
}
