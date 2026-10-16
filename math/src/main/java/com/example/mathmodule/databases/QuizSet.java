package com.example.mathmodule.databases;

/*
 * Role: Local data-layer class for Math, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public class QuizSet {
    private final int id;
    private final String title;
    private final String category;
    private final String difficulty;
    private final String educationLevel;

    /*
     * Responsibility: Initializes the `QuizSet` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public QuizSet(int id, String title, String category, String difficulty) {
        this(id, title, category, difficulty, "Middle School");
    }

    public QuizSet(int id, String title, String category, String difficulty,
                   String educationLevel) {
        this.id = id;
        this.title = title == null ? "Bộ đề Toán" : title;
        this.category = category == null ? "Khác" : category;
        this.difficulty = difficulty == null ? "Trung bình" : difficulty;
        this.educationLevel = educationLevel == null || educationLevel.trim().isEmpty()
                ? "Middle School" : educationLevel.trim();
    }

    /*
     * Responsibility: Reads or loads data for `get id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public String getEducationLevel() { return educationLevel; }
}
