package com.example.aimentor.science.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuizRequest {
    private final ScienceSubject subject;
    private final EducationLevel level;
    private final String topic;
    private final int numberOfQuestions;
    private final int userId;
    private final QuizChallenge challenge;
    private final QuizPerformance recentPerformance;
    private final long variationSeed;
    private final List<String> recentlyUsedPrompts;

    public QuizRequest(ScienceSubject subject, EducationLevel level,
                       String topic, int numberOfQuestions) {
        this(subject, level, topic, numberOfQuestions, 0, QuizChallenge.ON_LEVEL,
                QuizPerformance.empty(), System.nanoTime(), Collections.emptyList());
    }

    private QuizRequest(ScienceSubject subject, EducationLevel level,
                        String topic, int numberOfQuestions, int userId,
                        QuizChallenge challenge, QuizPerformance recentPerformance,
                        long variationSeed, List<String> recentlyUsedPrompts) {
        this.subject = subject == null ? ScienceSubject.PHYSICS : subject;
        this.level = level == null ? EducationLevel.MIDDLE_SCHOOL : level;
        this.topic = topic == null || topic.trim().isEmpty() ? "General science" : topic.trim();
        this.numberOfQuestions = Math.max(3, Math.min(5, numberOfQuestions));
        this.userId = Math.max(0, userId);
        this.challenge = challenge == null ? QuizChallenge.ON_LEVEL : challenge;
        this.recentPerformance = recentPerformance == null
                ? QuizPerformance.empty() : recentPerformance;
        this.variationSeed = variationSeed;
        this.recentlyUsedPrompts = recentlyUsedPrompts == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(recentlyUsedPrompts));
    }

    public QuizRequest withAdaptiveContext(int userId, QuizChallenge challenge,
                                           QuizPerformance performance, long variationSeed,
                                           List<String> recentlyUsedPrompts) {
        return new QuizRequest(subject, level, topic, numberOfQuestions, userId,
                challenge, performance, variationSeed, recentlyUsedPrompts);
    }

    public ScienceSubject getSubject() { return subject; }
    public EducationLevel getLevel() { return level; }
    public String getTopic() { return topic; }
    public int getNumberOfQuestions() { return numberOfQuestions; }
    public int getUserId() { return userId; }
    public QuizChallenge getChallenge() { return challenge; }
    public QuizPerformance getRecentPerformance() { return recentPerformance; }
    public long getVariationSeed() { return variationSeed; }
    public List<String> getRecentlyUsedPrompts() { return recentlyUsedPrompts; }
}
