package com.brightpath.science.domain.model;

/**
 * Immutable input for one science-AI request.
 *
 * questionText is the student's current message and is what is persisted in history.
 * conversationContext is optional transient context used only to make multi-turn chat coherent.
 */
public final class ScienceQuestion {
    private final int userId;
    private final ScienceSubject subject;
    private final EducationLevel educationLevel;
    private final ExplanationStyle explanationStyle;
    private final String questionText;
    private final String conversationContext;
    private final String conversationId;
    private final String imageUri;
    private final byte[] imageBytes;
    private final String imageMimeType;

        /*
 * Responsibility: Initializes ScienceQuestion and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceQuestion(int userId,
                           ScienceSubject subject,
                           EducationLevel educationLevel,
                           ExplanationStyle explanationStyle,
                           String questionText,
                           String imageUri,
                           byte[] imageBytes,
                           String imageMimeType) {
        this(userId, subject, educationLevel, explanationStyle, questionText, "", "",
                imageUri, imageBytes, imageMimeType);
    }

        /*
 * Responsibility: Initializes ScienceQuestion and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceQuestion(int userId,
                           ScienceSubject subject,
                           EducationLevel educationLevel,
                           ExplanationStyle explanationStyle,
                           String questionText,
                           String conversationContext,
                           String imageUri,
                           byte[] imageBytes,
                           String imageMimeType) {
        this(userId, subject, educationLevel, explanationStyle, questionText,
                conversationContext, "", imageUri, imageBytes, imageMimeType);
    }

        /*
 * Responsibility: Initializes ScienceQuestion and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceQuestion(int userId,
                           ScienceSubject subject,
                           EducationLevel educationLevel,
                           ExplanationStyle explanationStyle,
                           String questionText,
                           String conversationContext,
                           String conversationId,
                           String imageUri,
                           byte[] imageBytes,
                           String imageMimeType) {
        this.userId = userId;
        this.subject = subject;
        this.educationLevel = educationLevel;
        this.explanationStyle = explanationStyle;
        this.questionText = questionText == null ? "" : questionText.trim();
        this.conversationContext = conversationContext == null ? "" : conversationContext.trim();
        this.conversationId = conversationId == null ? "" : conversationId.trim();
        this.imageUri = imageUri;
        this.imageBytes = imageBytes;
        this.imageMimeType = imageMimeType;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getUserId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getUserId() { return userId; }
        /*
 * Responsibility: Retrieves the data/state required by `getSubject` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ScienceSubject getSubject() { return subject; }
        /*
 * Responsibility: Retrieves the data/state required by `getEducationLevel` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public EducationLevel getEducationLevel() { return educationLevel; }
        /*
 * Responsibility: Retrieves the data/state required by `getExplanationStyle` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public ExplanationStyle getExplanationStyle() { return explanationStyle; }
        /*
 * Responsibility: Retrieves the data/state required by `getQuestionText` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getQuestionText() { return questionText; }
        /*
 * Responsibility: Retrieves the data/state required by `getConversationContext` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getConversationContext() { return conversationContext; }
        /*
 * Responsibility: Retrieves the data/state required by `getConversationId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getConversationId() { return conversationId; }
        /*
 * Responsibility: Retrieves the data/state required by `getImageUri` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getImageUri() { return imageUri; }
        /*
 * Responsibility: Retrieves the data/state required by `getImageBytes` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public byte[] getImageBytes() { return imageBytes; }
        /*
 * Responsibility: Retrieves the data/state required by `getImageMimeType` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getImageMimeType() { return imageMimeType; }
        /*
 * Responsibility: Checks the condition represented by `hasImage` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean hasImage() { return imageBytes != null && imageBytes.length > 0; }
        /*
 * Responsibility: Checks the condition represented by `hasConversationContext` and returns the corresponding boolean result.
 * Purpose: Centralizes the decision rule so the same condition is evaluated consistently.
 */
public boolean hasConversationContext() { return !conversationContext.isEmpty(); }
}
