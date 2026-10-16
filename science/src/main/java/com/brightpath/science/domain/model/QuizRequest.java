package com.brightpath.science.domain.model;

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

        /*
 * Responsibility: Initializes QuizRequest and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public QuizRequest(ScienceSubject subject, EducationLevel level,
                       String topic, int numberOfQuestions) {
        this(subject, level, topic, numberOfQuestions, 0, QuizChallenge.ON_LEVEL,
                QuizPerformance.empty(), System.nanoTime(), Collections.emptyList());
    }

        /*
 * Responsibility: Initializes QuizRequest and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
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

        /*
 * Responsibility: Returns the `withAdaptiveContext` value stored by the QuizRequest domain model.
 * Purpose: Provides controlled read access to immutable/model state shared between Science layers.
 */
public QuizRequest withAdaptiveContext(int userId, QuizChallenge challenge,
                                           QuizPerformance performance, long variationSeed,
                                           List<String> recentlyUsedPrompts) {
        return new QuizRequest(subject, level, topic, numberOfQuestions, userId,
                challenge, performance, variationSeed, recentlyUsedPrompts);
    }

        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getSubject() { return subject; }
        /*
 * Responsibility: Retrieves the data/state required by `getLevel` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public EducationLevel getLevel() { return level; }
        /*
 * Responsibility: Retrieves the data/state required by `getTopic` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getTopic() { return topic; }
        /*
 * Responsibility: Retrieves the data/state required by `getNumberOfQuestions` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getNumberOfQuestions() { return numberOfQuestions; }
        /*
 * Responsibility: Retrieves the data/state required by `getUserId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getUserId() { return userId; }
        /*
 * Responsibility: Retrieves the data/state required by `getChallenge` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public QuizChallenge getChallenge() { return challenge; }
        /*
 * Responsibility: Retrieves the data/state required by `getRecentPerformance` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public QuizPerformance getRecentPerformance() { return recentPerformance; }
        /*
 * Responsibility: Retrieves the data/state required by `getVariationSeed` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getVariationSeed() { return variationSeed; }
        /*
 * Responsibility: Retrieves the data/state required by `getRecentlyUsedPrompts` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public List<String> getRecentlyUsedPrompts() { return recentlyUsedPrompts; }
}
