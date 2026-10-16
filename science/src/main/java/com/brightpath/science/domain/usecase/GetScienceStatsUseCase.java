package com.brightpath.science.domain.usecase;

import com.brightpath.science.domain.model.ScienceStats;
import com.brightpath.science.domain.repository.QuizAttemptRepository;
import com.brightpath.science.domain.repository.ScienceBookmarkRepository;
import com.brightpath.science.domain.repository.ScienceHistoryRepository;

public final class GetScienceStatsUseCase {
    private final ScienceHistoryRepository historyRepository;
    private final ScienceBookmarkRepository bookmarkRepository;
    private final QuizAttemptRepository quizAttemptRepository;

        /*
 * Responsibility: Initializes GetScienceStatsUseCase and receives or prepares the dependencies/state required by this application/business use-case.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public GetScienceStatsUseCase(ScienceHistoryRepository historyRepository,
                                  ScienceBookmarkRepository bookmarkRepository,
                                  QuizAttemptRepository quizAttemptRepository) {
        this.historyRepository = historyRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

        /*
 * Responsibility: Executes the main operation of GetScienceStatsUseCase using the supplied input and dependencies.
 * Purpose: Encapsulates the application/business use-case workflow so callers invoke one clear operation instead of coordinating internal steps.
 */
public ScienceStats execute(int userId) {
        return new ScienceStats(
                historyRepository.countByUser(userId),
                bookmarkRepository.countByUser(userId),
                historyRepository.mostFrequentSubject(userId),
                quizAttemptRepository.countAttempts(userId),
                quizAttemptRepository.calculateAccuracy(userId),
                historyRepository.totalReviewSeconds(userId),
                quizAttemptRepository.getTotalExp(userId)
        );
    }
}
