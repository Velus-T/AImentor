package com.example.mathmodule.databases;

/*
 * Role: Local data-layer class for Math, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public class MathNoteItem {
    private int id;
    private String title;
    private String formula;
    private String category;
    private int isPinned; // 0: Chưa ghim, 1: Đã ghim

    /*
     * Responsibility: Initializes the `MathNoteItem` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public MathNoteItem(int id, String title, String formula, String category, int isPinned) {
        this.id = id;
        this.title = title;
        this.formula = formula;
        this.category = category;
        this.isPinned = isPinned;
    }

    /*
     * Responsibility: Reads or loads data for `get id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getFormula() { return formula; }
    public String getCategory() { return category; }
    public int getIsPinned() { return isPinned; }
    /*
     * Responsibility: Applies or updates `set is pinned` in the current state or UI.
     * Purpose: A dedicated method ensures the same update behavior is used consistently by every caller.
     */
    public void setIsPinned(int isPinned) { this.isPinned = isPinned; }
}