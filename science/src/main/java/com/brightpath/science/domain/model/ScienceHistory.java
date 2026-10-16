package com.brightpath.science.domain.model;

public final class ScienceHistory {
    private final long id;
    private final int userId;
    private final String conversationId;
    private final ScienceSubject subject;
    private final String educationLevel;
    private final String explanationStyle;
    private final String question;
    private final String imageUri;
    private final String answer;
    private final long createdAt;
    private final int reviewSeconds;

        /*
 * Responsibility: Initializes ScienceHistory and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceHistory(long id, int userId, ScienceSubject subject, String educationLevel,
                          String explanationStyle, String question, String imageUri,
                          String answer, long createdAt, int reviewSeconds) {
        this(id, userId, "legacy-" + id, subject, educationLevel, explanationStyle,
                question, imageUri, answer, createdAt, reviewSeconds);
    }

        /*
 * Responsibility: Initializes ScienceHistory and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceHistory(long id, int userId, String conversationId, ScienceSubject subject,
                          String educationLevel, String explanationStyle, String question,
                          String imageUri, String answer, long createdAt, int reviewSeconds) {
        this.id = id;
        this.userId = userId;
        this.conversationId = conversationId == null || conversationId.trim().isEmpty()
                ? "legacy-" + id : conversationId.trim();
        this.subject = subject;
        this.educationLevel = educationLevel;
        this.explanationStyle = explanationStyle;
        this.question = question;
        this.imageUri = imageUri;
        this.answer = answer;
        this.createdAt = createdAt;
        this.reviewSeconds = reviewSeconds;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getId() { return id; }
        /*
 * Responsibility: Retrieves the data/state required by `getUserId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getUserId() { return userId; }
        /*
 * Responsibility: Retrieves the data/state required by `getConversationId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getConversationId() { return conversationId; }
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getSubject() { return subject; }
        /*
 * Responsibility: Retrieves the data/state required by `getEducationLevel` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getEducationLevel() { return educationLevel; }
        /*
 * Responsibility: Retrieves the data/state required by `getExplanationStyle` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getExplanationStyle() { return explanationStyle; }
        /*
 * Responsibility: Retrieves the data/state required by `getQuestion` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getQuestion() { return question; }
        /*
 * Responsibility: Retrieves the data/state required by `getImageUri` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getImageUri() { return imageUri; }
        /*
 * Responsibility: Retrieves the data/state required by `getAnswer` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getAnswer() { return answer; }
        /*
 * Responsibility: Retrieves the data/state required by `getCreatedAt` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public long getCreatedAt() { return createdAt; }
        /*
 * Responsibility: Retrieves the data/state required by `getReviewSeconds` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getReviewSeconds() { return reviewSeconds; }
}
