package com.brightpath.science.domain.model;

/**
 * Adjusts difficulty inside the selected education level.
 * It never changes the learner's selected level or introduces content from another level.
 */
public enum QuizChallenge {
    SUPPORT("Supported practice"),
    ON_LEVEL("On-level practice"),
    CHALLENGE("Challenge practice");

    private final String displayName;

        /*
 * Responsibility: Initializes QuizChallenge and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
QuizChallenge(String displayName) {
        this.displayName = displayName;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getDisplayName` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public String getDisplayName() {
        return displayName;
    }
}
