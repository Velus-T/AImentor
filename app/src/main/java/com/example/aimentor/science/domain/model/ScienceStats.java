package com.example.aimentor.science.domain.model;

public final class ScienceStats {
    private final int totalQuestions;
    private final int bookmarkCount;
    private final ScienceSubject mostStudiedSubject;
    private final int quizAttempts;
    private final double quizAccuracy;
    private final int reviewSeconds;

    public ScienceStats(int totalQuestions, int bookmarkCount, ScienceSubject mostStudiedSubject,
                        int quizAttempts, double quizAccuracy, int reviewSeconds) {
        this.totalQuestions = totalQuestions;
        this.bookmarkCount = bookmarkCount;
        this.mostStudiedSubject = mostStudiedSubject;
        this.quizAttempts = quizAttempts;
        this.quizAccuracy = quizAccuracy;
        this.reviewSeconds = reviewSeconds;
    }

    public int getTotalQuestions() { return totalQuestions; }
    public int getBookmarkCount() { return bookmarkCount; }
    public ScienceSubject getMostStudiedSubject() { return mostStudiedSubject; }
    public int getQuizAttempts() { return quizAttempts; }
    public double getQuizAccuracy() { return quizAccuracy; }
    public int getReviewSeconds() { return reviewSeconds; }
}
