package com.example.aimentor.science.domain.policy.impl;

import com.example.aimentor.science.domain.model.QuizChallenge;
import com.example.aimentor.science.domain.model.QuizPerformance;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.policy.AdaptiveQuizPolicy;
import com.example.aimentor.science.domain.repository.QuizAttemptRepository;
import com.example.aimentor.science.domain.repository.QuizVariationRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adapts challenge from recent accuracy while keeping the selected education level fixed.
 */
public final class PerformanceBasedQuizPolicy implements AdaptiveQuizPolicy {
    private final QuizAttemptRepository attemptRepository;
    private final QuizVariationRepository variationRepository;
    private final AtomicLong sequence = new AtomicLong(System.nanoTime());

    public PerformanceBasedQuizPolicy(QuizAttemptRepository attemptRepository,
                                      QuizVariationRepository variationRepository) {
        this.attemptRepository = attemptRepository;
        this.variationRepository = variationRepository;
    }

    @Override
    public QuizRequest adapt(int userId, QuizRequest baseRequest) {
        QuizPerformance performance;
        try {
            performance = attemptRepository.getRecentPerformance(
                    userId, baseRequest.getSubject(), baseRequest.getLevel(), 5);
        } catch (Exception ignored) {
            performance = new QuizPerformance(0, 0, 0);
        }
        QuizChallenge challenge = chooseChallenge(performance);
        List<String> recentPrompts;
        try {
            recentPrompts = variationRepository.getRecentPrompts(
                    userId, baseRequest.getSubject(), baseRequest.getLevel(), 12);
        } catch (Exception ignored) {
            recentPrompts = java.util.Collections.emptyList();
        }

        long seed = System.currentTimeMillis()
                ^ sequence.incrementAndGet()
                ^ ((long) userId << 32)
                ^ baseRequest.getSubject().ordinal() * 31L
                ^ baseRequest.getLevel().ordinal() * 131L;

        return baseRequest.withAdaptiveContext(
                userId, challenge, performance, seed, recentPrompts);
    }

    public QuizChallenge chooseChallenge(QuizPerformance performance) {
        if (performance == null || !performance.hasEnoughEvidence()) {
            return QuizChallenge.ON_LEVEL;
        }
        double accuracy = performance.getAccuracyPercent();
        if (accuracy < 50.0) return QuizChallenge.SUPPORT;
        if (accuracy >= 80.0) return QuizChallenge.CHALLENGE;
        return QuizChallenge.ON_LEVEL;
    }
}
