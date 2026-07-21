package com.example.aimentor.science.domain.model;

/**
 * Adjusts difficulty inside the selected education level.
 * It never changes the learner's selected level or introduces content from another level.
 */
public enum QuizChallenge {
    SUPPORT("Supported practice"),
    ON_LEVEL("On-level practice"),
    CHALLENGE("Challenge practice");

    private final String displayName;

    QuizChallenge(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
