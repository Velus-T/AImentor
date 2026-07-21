package com.example.aimentor.science.domain.usecase;

import com.example.aimentor.science.domain.model.ScienceStats;
import com.example.aimentor.science.domain.repository.QuizAttemptRepository;
import com.example.aimentor.science.domain.repository.ScienceBookmarkRepository;
import com.example.aimentor.science.domain.repository.ScienceHistoryRepository;

public final class GetScienceStatsUseCase {
    private final ScienceHistoryRepository historyRepository;
    private final ScienceBookmarkRepository bookmarkRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public GetScienceStatsUseCase(ScienceHistoryRepository historyRepository,
                                  ScienceBookmarkRepository bookmarkRepository,
                                  QuizAttemptRepository quizAttemptRepository) {
        this.historyRepository = historyRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public ScienceStats execute(int userId) {
        return new ScienceStats(
                historyRepository.countByUser(userId),
                bookmarkRepository.countByUser(userId),
                historyRepository.mostFrequentSubject(userId),
                quizAttemptRepository.countAttempts(userId),
                quizAttemptRepository.calculateAccuracy(userId),
                historyRepository.totalReviewSeconds(userId)
        );
    }
}
