package com.example.mathmodule.databases;

import com.example.mathmodule.util.MathQuizDifficulty;

/*
 * Role: Local data-layer class for Math, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public class QuizQuestion {
    private final int id;
    private final int setId;
    private final String questionText;
    private final String optionA;
    private final String optionB;
    private final String optionC;
    private final String optionD;
    private final String correctAnswer;
    private final String explanation;
    private final MathQuizDifficulty difficulty;
    private String selectedAnswer = "";

    /** Compatibility constructor for existing bundled quiz data. */
    /*
     * Responsibility: Initializes the `QuizQuestion` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public QuizQuestion(int id, int setId, String questionText, String optionA,
                        String optionB, String optionC, String optionD,
                        String correctAnswer, String explanation) {
        this(id, setId, questionText, optionA, optionB, optionC, optionD,
                correctAnswer, explanation, MathQuizDifficulty.MEDIUM);
    }

    /*
     * Responsibility: Initializes the `QuizQuestion` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public QuizQuestion(int id, int setId, String questionText, String optionA,
                        String optionB, String optionC, String optionD,
                        String correctAnswer, String explanation,
                        MathQuizDifficulty difficulty) {
        this.id = id;
        this.setId = setId;
        this.questionText = safe(questionText);
        this.optionA = safe(optionA);
        this.optionB = safe(optionB);
        this.optionC = safe(optionC);
        this.optionD = safe(optionD);
        this.correctAnswer = normalizeAnswer(correctAnswer);
        this.explanation = safe(explanation);
        this.difficulty = difficulty == null ? MathQuizDifficulty.MEDIUM : difficulty;
    }

    /*
     * Responsibility: Initializes the `QuizQuestion` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public QuizQuestion(int id, int setId, String questionText, String optionA,
                        String optionB, String optionC, String optionD,
                        String correctAnswer, String explanation, String difficulty) {
        this(id, setId, questionText, optionA, optionB, optionC, optionD,
                correctAnswer, explanation, MathQuizDifficulty.fromApiValue(difficulty));
    }

    /*
     * Responsibility: Performs the `safe` responsibility of `QuizQuestion` and returns or updates the result for the caller.
     * Purpose: A focused method improves readability, reuse, and step-by-step explanation.
     */
    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeAnswer(String value) {
        String answer = safe(value).toUpperCase();
        return answer.matches("[ABCD]") ? answer : "A";
    }

    /*
     * Responsibility: Reads or loads data for `get id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getId() { return id; }
    public int getSetId() { return setId; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    /*
     * Responsibility: Reads or loads data for `get option c` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
    public MathQuizDifficulty getDifficulty() { return difficulty; }
    /*
     * Responsibility: Reads or loads data for `get difficulty display name` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getDifficultyDisplayName() { return difficulty.getDisplayName(); }
    public int getExpReward() { return difficulty.getExpReward(); }
    public String getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = normalizeSelectedAnswer(selectedAnswer);
    }

    /*
     * Responsibility: Converts or normalizes data in `normalize selected answer` into the required format.
     * Purpose: A centralized conversion point keeps formatting consistent and makes invalid data easier to handle.
     */
    private static String normalizeSelectedAnswer(String value) {
        String answer = safe(value).toUpperCase();
        return answer.matches("[ABCD]") ? answer : "";
    }
}
