package com.example.aimentor.science.domain.model;

public final class ScienceBookmark {
    private final long id;
    private final int userId;
    private final long historyId;
    private final ScienceSubject subject;
    private final String title;
    private final String content;
    private final long createdAt;

    public ScienceBookmark(long id, int userId, long historyId, ScienceSubject subject,
                           String title, String content, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.historyId = historyId;
        this.subject = subject;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public int getUserId() { return userId; }
    public long getHistoryId() { return historyId; }
    public ScienceSubject getSubject() { return subject; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getCreatedAt() { return createdAt; }
}
