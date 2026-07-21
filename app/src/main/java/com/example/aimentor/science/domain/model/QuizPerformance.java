package com.example.aimentor.science.domain.model;

/** Immutable summary of recent attempts used by the adaptive quiz policy. */
public final class QuizPerformance {
    private final int attempts;
    private final int totalQuestions;
    private final int correctAnswers;

    public QuizPerformance(int attempts, int totalQuestions, int correctAnswers) {
        this.attempts = Math.max(0, attempts);
        this.totalQuestions = Math.max(0, totalQuestions);
        this.correctAnswers = Math.max(0, Math.min(correctAnswers, this.totalQuestions));
    }

    public static QuizPerformance empty() {
        return new QuizPerformance(0, 0, 0);
    }

    public int getAttempts() { return attempts; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }

    public double getAccuracyPercent() {
        return totalQuestions == 0 ? -1.0 : correctAnswers * 100.0 / totalQuestions;
    }

    public boolean hasEnoughEvidence() {
        return attempts >= 2 && totalQuestions >= 6;
    }
}
