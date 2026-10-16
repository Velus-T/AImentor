package com.brightpath.science.domain.model;

public final class ScienceStats {
    private final int totalQuestions;
    private final int bookmarkCount;
    private final ScienceSubject mostStudiedSubject;
    private final int quizAttempts;
    private final double quizAccuracy;
    private final int reviewSeconds;
    private final int totalScienceExp;

        /*
 * Responsibility: Initializes ScienceStats and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceStats(int totalQuestions, int bookmarkCount, ScienceSubject mostStudiedSubject,
                        int quizAttempts, double quizAccuracy, int reviewSeconds,
                        int totalScienceExp) {
        this.totalQuestions = totalQuestions;
        this.bookmarkCount = bookmarkCount;
        this.mostStudiedSubject = mostStudiedSubject;
        this.quizAttempts = quizAttempts;
        this.quizAccuracy = quizAccuracy;
        this.reviewSeconds = reviewSeconds;
        this.totalScienceExp = Math.max(0, totalScienceExp);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getTotalQuestions` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getTotalQuestions() { return totalQuestions; }
        /*
 * Responsibility: Retrieves the data/state required by `getBookmarkCount` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getBookmarkCount() { return bookmarkCount; }
        /*
 * Responsibility: Retrieves the data/state required by `getMostStudiedSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getMostStudiedSubject() { return mostStudiedSubject; }
        /*
 * Responsibility: Retrieves the data/state required by `getQuizAttempts` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getQuizAttempts() { return quizAttempts; }
        /*
 * Responsibility: Retrieves the data/state required by `getQuizAccuracy` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public double getQuizAccuracy() { return quizAccuracy; }
        /*
 * Responsibility: Retrieves the data/state required by `getReviewSeconds` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getReviewSeconds() { return reviewSeconds; }
        /*
 * Responsibility: Retrieves the data/state required by `getTotalScienceExp` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getTotalScienceExp() { return totalScienceExp; }
}
