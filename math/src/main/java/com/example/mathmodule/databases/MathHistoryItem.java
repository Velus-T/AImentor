package com.example.mathmodule.databases;

/*
 * Role: Local data-layer class for Math, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public class MathHistoryItem {
    private int id;
    private String question;
    private String answer;
    private String timestamp;

    /*
     * Responsibility: Initializes the `MathHistoryItem` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public MathHistoryItem() {
    }

    public MathHistoryItem(String question, String answer, String timestamp) {
        this.question = question;
        this.answer = answer;
        this.timestamp = timestamp;
    }

    /*
     * Responsibility: Reads or loads data for `get id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /*
     * Responsibility: Reads or loads data for `get question` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    /*
     * Responsibility: Reads or loads data for `get answer` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /*
     * Responsibility: Reads or loads data for `get timestamp` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}