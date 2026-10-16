package com.example.historymodule.databases;

/** Display model used for conversation summaries and quiz attempt summaries. */
/*
 * Role: Local data-layer class for History, responsible for SQLite and data read/write operations.
 * Purpose: Separating the database from the UI centralizes queries, improves maintainability, and reduces direct SQL dependencies in Activities and Fragments.
 */
public final class HistoryHistoryItem {
    private final long id;
    private final String referenceId;
    private final String title;
    private final String content;
    private final String timestamp;

    /*
     * Responsibility: Initializes the `HistoryHistoryItem` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public HistoryHistoryItem(long id, String title, String content, String timestamp) {
        this(id, null, title, content, timestamp);
    }

    public HistoryHistoryItem(long id,
                              String referenceId,
                              String title,
                              String content,
                              String timestamp) {
        this.id = id;
        this.referenceId = referenceId;
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.timestamp = timestamp == null ? "" : timestamp;
    }

    /*
     * Responsibility: Reads or loads data for `get id` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public long getId() { return id; }
    public String getReferenceId() { return referenceId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
}
