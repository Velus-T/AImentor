package com.example.asm_final.utils;

/*
 * Role: Utility class for App Shell, grouping reusable logic such as user sessions, levels, formatting, or data conversion.
 * Purpose: Moving shared logic into helpers reduces duplication and keeps Activities and Fragments focused on screen coordination.
 */
public class RecentActivity {
    public enum Type { CHAT, QUIZ }
    public enum Subject { MATH, SCIENCE, HISTORY, PROGRAMMING }

    private final Subject subject;
    private final Type type;
    private final String title;
    private final String description;
    private final long timestamp;

    /*
     * Responsibility: Initializes the `RecentActivity` object and sets up the required dependencies and initial data.
     * Purpose: The constructor ensures the object starts in a valid state immediately after creation.
     */
    public RecentActivity(Subject subject, Type type, String title, String description, long timestamp) {
        this.subject = subject;
        this.type = type;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    /*
     * Responsibility: Reads or loads data for `get subject` from the appropriate data source.
     * Purpose: Centralizing read operations makes null handling, error handling, and data sources clearer.
     */
    public Subject getSubject() { return subject; }
    public Type getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
}
