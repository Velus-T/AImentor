package com.example.aimentor.science.domain.model;

public final class ScienceHistory {
    private final long id;
    private final int userId;
    private final ScienceSubject subject;
    private final String educationLevel;
    private final String explanationStyle;
    private final String question;
    private final String imageUri;
    private final String answer;
    private final long createdAt;
    private final int reviewSeconds;

    public ScienceHistory(long id, int userId, ScienceSubject subject, String educationLevel,
                          String explanationStyle, String question, String imageUri,
                          String answer, long createdAt, int reviewSeconds) {
        this.id = id;
        this.userId = userId;
        this.subject = subject;
        this.educationLevel = educationLevel;
        this.explanationStyle = explanationStyle;
        this.question = question;
        this.imageUri = imageUri;
        this.answer = answer;
        this.createdAt = createdAt;
        this.reviewSeconds = reviewSeconds;
    }

    public long getId() { return id; }
    public int getUserId() { return userId; }
    public ScienceSubject getSubject() { return subject; }
    public String getEducationLevel() { return educationLevel; }
    public String getExplanationStyle() { return explanationStyle; }
    public String getQuestion() { return question; }
    public String getImageUri() { return imageUri; }
    public String getAnswer() { return answer; }
    public long getCreatedAt() { return createdAt; }
    public int getReviewSeconds() { return reviewSeconds; }
}
