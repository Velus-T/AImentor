package com.example.historymodule.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Role: Model class for History, representing data exchanged between the UI, database, and/or AI layer.
 * Purpose: The model provides a clear data structure, avoids passing many separate values, and gives different layers a shared format.
 */
public final class HistoryQuizQuestion {
    private final String difficulty;
    private final String question;
    private final List<String> options;
    private final int correctIndex;
    private final String explanation;

    /*
     * Responsibility: Initializes the `HistoryQuizQuestion` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public HistoryQuizQuestion(String difficulty,
                               String question,
                               List<String> options,
                               int correctIndex,
                               String explanation) {
        this.difficulty = normalizeDifficulty(difficulty);
        this.question = question == null ? "" : question.trim();
        this.options = Collections.unmodifiableList(
                options == null ? new ArrayList<>() : new ArrayList<>(options));
        this.correctIndex = Math.max(0, Math.min(3, correctIndex));
        this.explanation = explanation == null ? "" : explanation.trim();
    }

    /*
     * Responsibility: Converts or normalizes data in `normalize difficulty` into the required format.
     * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
     */
    private static String normalizeDifficulty(String value) {
        if (value == null) return "MEDIUM";
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if ("EASY".equals(normalized) || "HARD".equals(normalized)) {
            return normalized;
        }
        return "MEDIUM";
    }

    /*
     * Responsibility: Reads or loads data for `get difficulty` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getDifficulty() {
        return difficulty;
    }

    public String getDifficultyDisplayName() {
        if ("EASY".equals(difficulty)) return "Dễ";
        if ("HARD".equals(difficulty)) return "Khó";
        return "Trung bình";
    }

    /*
     * Responsibility: Reads or loads data for `get exp reward` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getExpReward() { return com.example.mentor.data.QuizPolicy.reward(difficulty);
    }

    /*
     * Responsibility: Reads or loads data for `get question` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    /*
     * Responsibility: Reads or loads data for `get correct index` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getExplanation() {
        return explanation;
    }
}
