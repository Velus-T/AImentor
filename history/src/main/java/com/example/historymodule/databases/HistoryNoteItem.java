package com.example.historymodule.databases;

/*
 * Role: Local data-layer class for History, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public final class HistoryNoteItem {
    private final long id;
    private final String title;
    private final String content;
    private final boolean pinned;
    private final long updatedAt;

    /*
     * Responsibility: Initializes the `HistoryNoteItem` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public HistoryNoteItem(long id,
                           String title,
                           String content,
                           boolean pinned,
                           long updatedAt) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.pinned = pinned;
        this.updatedAt = Math.max(0L, updatedAt);
    }

    /*
     * Responsibility: Reads or loads data for `get id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isPinned() { return pinned; }
    public long getUpdatedAt() { return updatedAt; }
}
