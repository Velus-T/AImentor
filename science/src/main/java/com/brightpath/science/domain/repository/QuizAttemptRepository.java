package com.brightpath.science.domain.repository;

import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.QuizChallenge;
import com.brightpath.science.domain.model.QuizPerformance;
import com.brightpath.science.domain.model.ScienceSubject;

public interface QuizAttemptRepository {
        /*
 * Responsibility: Persists the data supplied to `saveAttempt` through the appropriate Science storage/repository.
 * Purpose: Keeps important learner state available after navigation or application restart.
 */
long saveAttempt(int userId, ScienceSubject subject, EducationLevel level,
                     String topic, QuizChallenge challenge,
                     int totalQuestions, int correctAnswers, int expEarned);

        /*
 * Responsibility: Retrieves the data/state required by `getRecentPerformance` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
QuizPerformance getRecentPerformance(int userId, ScienceSubject subject,
                                         EducationLevel level, int attemptLimit);

    /**
     * Returns the complete Quiz performance of one subject across every education level.
     * This is used by the Quiz dashboard and must never mix Physics, Chemistry and Biology.
     */
        /*
 * Responsibility: Retrieves the data/state required by `getSubjectPerformance` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
QuizPerformance getSubjectPerformance(int userId, ScienceSubject subject);

        /*
 * Responsibility: Performs the repository operation `countAttempts` against the Science data source.
 * Purpose: Keeps persistence/query details behind the repository abstraction and away from presentation/business code.
 */
int countAttempts(int userId);
        /*
 * Responsibility: Calculates the value represented by `calculateAccuracy` from the supplied Science learning data.
 * Purpose: Keeps the calculation rule deterministic and reusable outside the UI.
 */
double calculateAccuracy(int userId);
        /*
 * Responsibility: Retrieves the data/state required by `getTotalExp` from the appropriate data repository.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
int getTotalExp(int userId);
}
