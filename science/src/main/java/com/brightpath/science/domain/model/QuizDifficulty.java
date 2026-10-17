package com.brightpath.science.domain.model;

import com.brightpath.science.R;

/** Difficulty of one quiz question and the EXP awarded for a correct answer. */
public enum QuizDifficulty {
    EASY(R.string.science_quiz_diff_easy, 5),
    MEDIUM(R.string.science_quiz_diff_medium, 10),
    HARD(R.string.science_quiz_diff_hard, 20);

    private final int displayNameResId;
    private final int expReward;

        /*
 * Responsibility: Initializes QuizDifficulty and receives or prepares the dependencies/state required by this domain model.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
QuizDifficulty(int displayNameResId, int expReward) {
        this.displayNameResId = displayNameResId;
        this.expReward = expReward;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getDisplayNameResId` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getDisplayNameResId() {
        return displayNameResId;
    }

        /*
 * Responsibility: Retrieves the data/state required by `getExpReward` from the appropriate domain model.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
public int getExpReward() {
        return com.example.mentor.data.QuizPolicy.reward(name());
    }

        /*
 * Responsibility: Converts input data in `fromApiValue` into the domain/model representation used by the Science Module.
 * Purpose: Keeps parsing/mapping separate from UI and storage code, producing consistent structured data.
 */
public static QuizDifficulty fromApiValue(String value) {
        if (value == null) return MEDIUM;
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if ("EASY".equals(normalized) || "DỄ".equals(normalized)) return EASY;
        if ("HARD".equals(normalized) || "KHÓ".equals(normalized)) return HARD;
        return MEDIUM;
    }

        /*
 * Responsibility: Returns the `inferFromType` value stored by the QuizDifficulty domain model.
 * Purpose: Provides controlled read access to immutable/model state shared between Science layers.
 */
public static QuizDifficulty inferFromType(QuizQuestion.Type type) {
        if (type == QuizQuestion.Type.TRUE_FALSE) return EASY;
        if (type == QuizQuestion.Type.MATCHING) return HARD;
        return MEDIUM;
    }
}
