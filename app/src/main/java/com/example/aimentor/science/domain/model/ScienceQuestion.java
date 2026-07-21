package com.example.aimentor.science.domain.model;

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
    private final String imageUri;
    private final byte[] imageBytes;
    private final String imageMimeType;

    public ScienceQuestion(int userId,
                           ScienceSubject subject,
                           EducationLevel educationLevel,
                           ExplanationStyle explanationStyle,
                           String questionText,
                           String imageUri,
                           byte[] imageBytes,
                           String imageMimeType) {
        this(userId, subject, educationLevel, explanationStyle, questionText, "",
                imageUri, imageBytes, imageMimeType);
    }

    public ScienceQuestion(int userId,
                           ScienceSubject subject,
                           EducationLevel educationLevel,
                           ExplanationStyle explanationStyle,
                           String questionText,
                           String conversationContext,
                           String imageUri,
                           byte[] imageBytes,
                           String imageMimeType) {
        this.userId = userId;
        this.subject = subject;
        this.educationLevel = educationLevel;
        this.explanationStyle = explanationStyle;
        this.questionText = questionText == null ? "" : questionText.trim();
        this.conversationContext = conversationContext == null ? "" : conversationContext.trim();
        this.imageUri = imageUri;
        this.imageBytes = imageBytes;
        this.imageMimeType = imageMimeType;
    }

    public int getUserId() { return userId; }
    public ScienceSubject getSubject() { return subject; }
    public EducationLevel getEducationLevel() { return educationLevel; }
    public ExplanationStyle getExplanationStyle() { return explanationStyle; }
    public String getQuestionText() { return questionText; }
    public String getConversationContext() { return conversationContext; }
    public String getImageUri() { return imageUri; }
    public byte[] getImageBytes() { return imageBytes; }
    public String getImageMimeType() { return imageMimeType; }
    public boolean hasImage() { return imageBytes != null && imageBytes.length > 0; }
    public boolean hasConversationContext() { return !conversationContext.isEmpty(); }
}
