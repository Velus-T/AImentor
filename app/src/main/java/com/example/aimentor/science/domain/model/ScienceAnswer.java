package com.example.aimentor.science.domain.model;

public final class ScienceAnswer {
    private final String content;
    private final String detectedSubject;
    private final String difficulty;
    private final boolean fromCache;
    private final long historyId;

    public ScienceAnswer(String content, String detectedSubject, String difficulty,
                         boolean fromCache, long historyId) {
        this.content = content == null ? "" : content;
        this.detectedSubject = detectedSubject == null ? "" : detectedSubject;
        this.difficulty = difficulty == null ? "" : difficulty;
        this.fromCache = fromCache;
        this.historyId = historyId;
    }

    public String getContent() { return content; }
    public String getDetectedSubject() { return detectedSubject; }
    public String getDifficulty() { return difficulty; }
    public boolean isFromCache() { return fromCache; }
    public long getHistoryId() { return historyId; }

    public ScienceAnswer withPersistence(long newHistoryId, boolean cached) {
        return new ScienceAnswer(content, detectedSubject, difficulty, cached, newHistoryId);
    }
}
