package com.brightpath.science.domain.model;

/** Immutable summary of recent attempts used by the adaptive quiz policy. */
public final class QuizPerformance {
    private final int attempts;
    private final int totalQuestions;
    private final int correctAnswers;

        /*
 * Responsibility: Initializes QuizPerformance and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public QuizPerformance(int attempts, int totalQuestions, int correctAnswers) {
        this.attempts = Math.max(0, attempts);
        this.totalQuestions = Math.max(0, totalQuestions);
        this.correctAnswers = Math.max(0, Math.min(correctAnswers, this.totalQuestions));
    }

        /*
 * Responsibility: Returns the `empty` value stored by the QuizPerformance domain model.
 * Purpose: Provides controlled read access to immutable/model state shared between Science layers.
 */
public static QuizPerformance empty() {
        return new QuizPerformance(0, 0, 0);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getAttempts` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getAttempts() { return attempts; }
        /*
 * Responsibility: Retrieves the data/state required by `getTotalQuestions` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getTotalQuestions() { return totalQuestions; }
        /*
 * Responsibility: Retrieves the data/state required by `getCorrectAnswers` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getCorrectAnswers() { return correctAnswers; }

        /*
 * Responsibility: Retrieves the data/state required by `getAccuracyPercent` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public double getAccuracyPercent() {
        return totalQuestions == 0 ? -1.0 : correctAnswers * 100.0 / totalQuestions;
    }

        /*
 * Responsibility: Checks the condition represented by `hasEnoughEvidence` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean hasEnoughEvidence() {
        return attempts >= 2 && totalQuestions >= 6;
    }
}
