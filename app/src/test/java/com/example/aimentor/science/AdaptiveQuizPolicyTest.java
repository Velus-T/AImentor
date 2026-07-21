package com.example.aimentor.science;

import com.example.aimentor.science.domain.model.EducationLevel;
import com.example.aimentor.science.domain.model.QuizChallenge;
import com.example.aimentor.science.domain.model.QuizPerformance;
import com.example.aimentor.science.domain.model.QuizRequest;
import com.example.aimentor.science.domain.model.ScienceSubject;
import com.example.aimentor.science.domain.policy.impl.PerformanceBasedQuizPolicy;
import com.example.aimentor.science.domain.repository.QuizAttemptRepository;
import com.example.aimentor.science.domain.repository.QuizVariationRepository;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AdaptiveQuizPolicyTest {
    @Test
    public void adaptsChallengeWithoutChangingEducationLevel() {
        FakeAttempts attempts = new FakeAttempts(new QuizPerformance(3, 10, 9));
        FakeVariations variations = new FakeVariations();
        PerformanceBasedQuizPolicy policy = new PerformanceBasedQuizPolicy(attempts, variations);
        QuizRequest base = new QuizRequest(
                ScienceSubject.CHEMISTRY, EducationLevel.HIGH_SCHOOL, "Acids", 5);

        QuizRequest first = policy.adapt(7, base);
        QuizRequest second = policy.adapt(7, base);

        assertEquals(EducationLevel.HIGH_SCHOOL, first.getLevel());
        assertEquals(QuizChallenge.CHALLENGE, first.getChallenge());
        assertEquals(Arrays.asList("Old question"), first.getRecentlyUsedPrompts());
        assertNotEquals(first.getVariationSeed(), second.getVariationSeed());
    }

    @Test
    public void lowAccuracySelectsSupportMode() {
        PerformanceBasedQuizPolicy policy = new PerformanceBasedQuizPolicy(
                new FakeAttempts(new QuizPerformance(2, 8, 3)), new FakeVariations());
        assertEquals(QuizChallenge.SUPPORT,
                policy.adapt(1, new QuizRequest(ScienceSubject.PHYSICS,
                        EducationLevel.MIDDLE_SCHOOL, "Motion", 3)).getChallenge());
    }

    @Test
    public void repositoryFailureFallsBackWithoutChangingLevel() {
        QuizAttemptRepository failingAttempts = new QuizAttemptRepository() {
            @Override public long saveAttempt(int userId, ScienceSubject subject, EducationLevel level,
                                              String topic, QuizChallenge challenge,
                                              int totalQuestions, int correctAnswers) { throw new RuntimeException("db"); }
            @Override public QuizPerformance getRecentPerformance(int userId, ScienceSubject subject,
                                                                   EducationLevel level, int attemptLimit) {
                throw new RuntimeException("db");
            }
            @Override public int countAttempts(int userId) { throw new RuntimeException("db"); }
            @Override public double calculateAccuracy(int userId) { throw new RuntimeException("db"); }
        };
        QuizVariationRepository failingVariations = new QuizVariationRepository() {
            @Override public List<String> getRecentPrompts(int userId, ScienceSubject subject,
                                                           EducationLevel level, int limit) {
                throw new RuntimeException("prefs");
            }
            @Override public void rememberPrompts(int userId, ScienceSubject subject,
                                                  EducationLevel level, List<String> prompts) {
                throw new RuntimeException("prefs");
            }
        };
        QuizRequest result = new PerformanceBasedQuizPolicy(failingAttempts, failingVariations)
                .adapt(1, new QuizRequest(ScienceSubject.BIOLOGY,
                        EducationLevel.UNIVERSITY, "Genetics", 5));

        assertEquals(EducationLevel.UNIVERSITY, result.getLevel());
        assertEquals(QuizChallenge.ON_LEVEL, result.getChallenge());
        assertTrue(result.getRecentlyUsedPrompts().isEmpty());
    }

    private static final class FakeAttempts implements QuizAttemptRepository {
        private final QuizPerformance performance;
        private FakeAttempts(QuizPerformance performance) { this.performance = performance; }
        @Override public long saveAttempt(int userId, ScienceSubject subject, EducationLevel level,
                                          String topic, QuizChallenge challenge,
                                          int totalQuestions, int correctAnswers) { return 1; }
        @Override public QuizPerformance getRecentPerformance(int userId, ScienceSubject subject,
                                                               EducationLevel level, int attemptLimit) {
            return performance;
        }
        @Override public int countAttempts(int userId) { return 0; }
        @Override public double calculateAccuracy(int userId) { return 0; }
    }

    private static final class FakeVariations implements QuizVariationRepository {
        @Override public List<String> getRecentPrompts(int userId, ScienceSubject subject,
                                                       EducationLevel level, int limit) {
            return Collections.singletonList("Old question");
        }
        @Override public void rememberPrompts(int userId, ScienceSubject subject,
                                              EducationLevel level, List<String> prompts) {
            assertTrue(prompts != null);
        }
    }
}
